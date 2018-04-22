# solitaire-player
Solve and automatically play Pyramid Solitaire.  Watch the [demonstration video](https://youtu.be/2fs49YDfUR4).


## Introduction

### Purpose
solitaire-player is a Java program that can quickly find an optimal solution to Pyramid Solitaire for any deck of cards you give it, if a solution exists.  Also, it can automatically play Pyramid Solitaire in Windows 10's Microsoft Solitaire Collection.

### Features
- Always finds a solution with the fewest possible number of steps, if there exists any solution at all.
- Supports 3 types of Pyramid Solitaire challenges:
  - Board Challenges: clear the board (remove the 28 cards in the pyramid layout in the game), even if there are still cards remaining in the stock or waste piles.
  - Card Challenges: remove a number of cards of a given rank.  For example, "Clear 12 Aces in 2 deals".
  - Score Challenges: reach a given score, or just maximize the score.
- Can automatically play Pyramid Solitaire in Microsoft Solitaire Collection on Windows 10.
  - You just need to have the app running at the beginning of a Pyramid Solitaire game, and tell it which type of challenge you are playing.
  - Supports 100%, 200%, and 250% scaling sizes (in Windows Display Settings).
  - Scans the cards automatically so you don't have to tell the program what they are.
- Can solve a deck of cards you give it, printing the solutions without automatically playing anything.

### Performance
The following performance measurements were done on an Intel i7-4770k CPU (3.5GHz, 3.9GHz max) running Windows 10.  They measure the time it takes to find a solution given a deck of cards, but do not include the time it takes to play the game.

#### Board Challenges
Here are timings for clearing the board on 1500 random decks of cards.  502 of them have no possible way to clear the board and the timing measurement is how long the program takes to find out there's no solution.

| Decks                | Mean (ms) | Median (ms) | Maximum (ms) | Total (hrs:min:sec) |
|:-------------------- | ---------:| -----------:| ------------:| -------------------:|
| 1500 random decks    |       598 |         156 |        33093 |             0:14:56 |
| 998 solvable decks   |       296 |         157 |         4547 |             0:04:55 |
| 502 unsolvable decks |      1198 |          47 |        33093 |             0:10:01 |

#### Score Challenges
Here are timings for finding out how to get the maximum possible score on the same 1500 random decks.

| Decks                | Mean (ms) | Median (ms) | Maximum (ms) | Total (hrs:min:sec) |
|:-------------------- | ---------:| -----------:| ------------:| -------------------:|
| 1500 random decks    |      6224 |        3804 |        76331 |             2:35:36 |

#### Card Challenges
Here are timings for finding out how to remove as many Aces as possible from the game.  It will find out the best possible solution while clearing the board, and the best solution which does not clear the board, and compare them.

| Decks                | Mean (ms) | Median (ms) | Maximum (ms) | Total (hrs:min:sec) |
|:-------------------- | ---------:| -----------:| ------------:| -------------------:|
| 1500 random decks    |      6198 |        3610 |        67427 |             2:34:57 |

## Usage

### Command Line program

#### Requirements
- [64 bit JRE 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
- usually the program only needs a few hundred MB of RAM, but sometimes it needs a few GB
- Windows - untested as of yet on other operating systems

#### Steps for automatically playing Pyramid Solitaire
1. Download the release and unzip it.
2. Go to solitaire-player/bin.
3. Start Microsoft Solitaire Collection on Windows 10.
4. In Microsoft Solitaire Collection start a Pyramid Solitaire game - either a regular game or a challenge game.
5. Run solitaire-player.bat with one of the following set of command line options:
  - `solitaire-player.bat Pyramid Board`
    - This runs the Board Challenge solver - clear the board in as few steps as possible.
  - `solitaire-player.bat Pyramid Score`
    - This runs the Score Challenge solver - maximize the score in as few steps as possible.
  - `solitaire-player.bat Pyramid Score <goal score> <current score>`
    - For example: "solitaire-player.bat Pyramid Score 2400 2240" means your current score is 2240 and if you reach 2400 points you win.
    - This runs the Score Challenge solver but knows it can stop playing if it reaches the goal score.
  - `solitaire-player.bat Pyramid Card <number of cards to remove> <card rank to remove> <current cards of that rank removed>`
    - Card ranks are one of the letters/numbers A 2 3 4 5 6 7 8 9 T J Q K.
    - For example: "solitaire-player.bat Pyramid Card 12 A 9" means the goal is to remove 12 Aces, and you've removed 9 so far in previous games.
    - This runs the Card Challenge solver which tries to find the shortest way to remove a number of cards of a given rank.
    
#### Steps for solving a deck you give it
1. Create a file containing a deck of 52 cards that looks like this as an example.  Cards are two letters containing rank (A23456789TJQK) followed by suit (cdhs):

```
            6d
          5h  Ah
        Jd  4s  Ks
      6s  8c  2h  4d
    9s  Kd  6c  Ad  8s
  Ac  5c  9d  7h  3h  8d
5s  4c  Qc  Jh  Kc  Kh  3c
3s 9c As 5d Qh Ts 4h 7s Td 9h Th 7c 8h 2c 7d Tc 2d 6h 2s Js Qd 3d Qs Jc
```
In the example above, the 3s is the initial card at the top of the stock pile and the Jc is the bottom of the stock pile.

2. Follow steps 1, 2, and 5 from the `Steps for automatically playing Pyramid Solitaire` instructions, except in step 5, always add `-f <filename>` to give the program the deck of cards to solve.  It will skip any automation and just print out the solutions it finds.

### Source Code
This is a standard gradle project so you can run things like "gradlew distZip" and run the program using the distribution.  The only unusual thing is the Windows version of the SikuliX jar in the lib/ directory.
There is also a [Programming Guide](programming.md) with details for developers.
