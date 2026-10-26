# Martian Robots

A solution to the Red Badger "Martian Robots" problem, written in Java 17 with no runtime
dependencies. Robots are dropped onto a bounded grid, follow a string of instructions from
Earth, and report where they ended up. Robots that drive off the edge are lost, but leave a
scent that stops later robots falling off at the same point.

## Requirements

- JDK 17 or newer

Maven does not need to be installed; the included wrapper (`mvnw`) downloads the right version
on first use.

## Running it

Build the jar (this also runs the tests):

```bash
./mvnw package
```

There are three ways to run it.

**Interactive.** Start it with no arguments and it asks how you want to provide the mission:
either the path to an input file, or step by step, where it prompts for the grid and then each
robot's position and instructions. Each answer is checked as you enter it, so a typo can be
fixed straight away instead of failing the whole run.

```bash
java -jar target/martian-robots.jar
```

```
Martian Robots

How would you like to provide the mission?
  1) Load it from a file
  2) Enter it step by step
Choose 1 or 2: 2

Enter the mission one line at a time.
Orientations are N, E, S or W. Instructions are L (turn left), R (turn right) and F (forward).

Grid size, as the upper-right corner (e.g. 5 3): 5 3
Robot 1 start position (e.g. 1 1 E), or press Enter to finish: 1 1 E
Robot 1 instructions (e.g. RFRFRFRF): RFRFRFRF
Robot 2 start position (e.g. 1 1 E), or press Enter to finish:

Final positions:
1 1 E
```

**From a file.** Pass the path as the only argument:

```bash
java -jar target/martian-robots.jar samples/sample-input.txt
```

**From standard input.** Pass `-` and pipe the input in:

```bash
java -jar target/martian-robots.jar - < samples/sample-input.txt
```

The file and standard input modes print the results and nothing else, so they are easy to use
from scripts. With the sample input from the brief they print:

```
1 1 E
3 3 N LOST
2 3 S
```

On Windows use `mvnw.cmd` in place of `./mvnw`.

If a file or piped input is invalid nothing is simulated. The program prints the line number
and the reason to standard error and exits with status `1`:

```
Invalid input. Line 3: unknown instruction 'Z' at column 3
```

## Running the tests

```bash
./mvnw test
```

The tests are plain JUnit 5. `MartianRobotsAppTest` is the end-to-end suite and includes the
sample from the brief. `InteractiveSessionTest` drives the interactive mode with scripted
answers. The other test classes cover each component on its own.

## How it works

The code is split into four small packages, each with one job:

| Package   | Responsibility                                                                       |
|-----------|--------------------------------------------------------------------------------------|
| `model`   | The domain: `Orientation`, `Coordinate`, `Position`, `Grid`, `Robot` and `Mars`      |
| `command` | The instructions a robot understands (`L`, `R`, `F`) and the `CommandRegistry`       |
| `mission` | `MissionControl`, which runs each robot in turn against a shared `Mars`              |
| `input`   | `MissionParser`, which validates the text input and turns it into a `Mission`        |

`MartianRobotsApp` wires these together and handles the command line. `InteractiveSession`
runs the prompts for interactive mode. It validates each answer with the same `MissionParser`
methods used for files, so the rules live in one place.

The flow for a single run is:

1. `MissionParser` reads the grid size and each robot's start position and instructions. Every
   instruction letter is looked up in the `CommandRegistry`, so unknown letters are caught here,
   before anything moves.
2. `MissionControl` creates a fresh `Mars` for the mission and runs the robots one after
   another. A robot stops processing instructions as soon as it is lost.
3. Each robot's final state is formatted as `x y O`, with ` LOST` appended where relevant.

A few decisions worth calling out:

- **Adding new commands.** The brief mentions that more command types may be needed later.
  Each command implements a one-method `Command` interface and is registered against a letter
  in `CommandRegistry`. Supporting, say, a `B` (move backwards) instruction means writing one
  class and adding one entry; the parser and simulation stay untouched.
  `CommandRegistryTest` and `MissionParserTest` both show this with a custom command.
- **Immutable values, one piece of mutable state.** `Position` and `Robot` are immutable records,
  so each command returns a new robot rather than changing one in place. The only mutable state
  is the set of scents held by `Mars`, because that is genuinely the only thing shared between
  robots.
- **Fail fast on bad input.** All validation lives in the parser and reports the original line
  number (blank lines are skipped but still counted), so a typo in robot 40 is not discovered
  after robots 1 to 39 have already moved.
- **Interactive and scripted modes.** Running with no arguments starts the interactive mode,
  which is friendlier for someone trying it out. Reading piped input therefore needs `-`
  (a common command-line convention), because otherwise there is no reliable way to tell
  whether a person is typing or input is being piped in.

## Assumptions

The brief leaves a few things open. These are the interpretations I went with:

- A scent belongs to a grid point, not to a direction. A robot lost going north from a corner
  also stops a later robot from leaving that corner going east.
- A robot's starting position must be on the grid, otherwise the input is rejected.
- Grid bounds and coordinates must be between 0 and 50 inclusive. A `0 0` grid (a single point)
  is valid.
- "Less than 100 characters" is enforced literally, so 99 is the longest allowed instruction
  string.
- Instructions and orientations are case-sensitive (`F` is valid, `f` is not), matching the
  brief.
- Blank lines and extra whitespace are ignored, so robots can be separated for readability.
- A grid line with no robots after it is valid and produces no output.

## What I would improve next

- **Configurable rules.** The 50-coordinate and 100-character limits are constants. If the
  rules were likely to change they could move into a small settings object passed to the parser.
- **Richer reporting.** Right now only the final position is reported. Recording each robot's
  path would make it easy to add a verbose mode, or to render the grid for debugging.
- **Collisions.** Robots run one at a time and never meet. If they ever ran concurrently, or
  stayed on the grid after finishing, `Mars` would need to track occupied points too.
- **Streaming input.** The whole input is read into memory before parsing. That's fine for the
  sizes in the brief, but a very large transmission could be parsed and run robot by robot.
- **Static analysis and CI.** I would add Checkstyle or Spotless for formatting, plus a GitHub
  Actions workflow that runs `./mvnw verify` on each push.
