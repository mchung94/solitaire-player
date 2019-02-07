# solitaire-player
Solve and automatically play Pyramid or TriPeaks Solitaire.  Watch the [demonstration video for Pyramid Solitaire](https://youtu.be/2fs49YDfUR4).  The latest release for Windows 10 for download is [version 2.0.0](https://github.com/mchung94/solitaire-player/releases/download/v2.0.0/solitaire-player-v2.0.0.zip), but [version 1.2.0](https://github.com/mchung94/solitaire-player/releases/download/v1.2.0/solitaire-player-v1.2.0.zip) is still available.  This requires that you install the [64 bit JRE 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html) first.


## Introduction

### Purpose
solitaire-player is a Java program that can quickly find an optimal solution to Pyramid or TriPeaks Solitaire for any deck of cards you give it, if a solution exists.  Also, it can automatically play Pyramid and TriPeaks Solitaire in Windows 10's Microsoft Solitaire Collection.

### Features
- Always finds a solution with the fewest possible number of steps, if there exists any solution at all.
- Supports 3 types of Pyramid and TriPeaks Solitaire challenges:
  - Board Challenges: clear the board, even if there are still cards remaining in the stock or waste piles.
  - Card Challenges: remove a number of cards of a given rank.  For example, "Clear 12 Aces in 2 deals".
  - Score Challenges: reach a given score, or just maximize the score.
- In Pyramid Solitaire it's easy to find out all 52 cards in the game by looking through the stock pile and then restart the game from the beginning.  But in TriPeaks Solitaire, there are 18 face down cards that can only be revealed by playing the game and removing the cards blocking them from below.  This program can play to reveal the face down cards.
- Can automatically play Pyramid or TriPeaks Solitaire in Microsoft Solitaire Collection on Windows 10.
  - You just need to have the app running at the beginning of a Pyramid or TriPeaks Solitaire game, and tell it which type of challenge you are playing.
  - Supports 100% scaling sizes only (in Windows Display Settings).  It used to support 200% or 250% but I can no longer support this.
  - Scans the cards automatically so you don't have to tell the program what they are.
- Can solve a deck of cards you give it, printing the solutions without automatically playing anything.

### Performance
The following performance measurements were done on an Intel i7-4770k CPU (3.5GHz, 3.9GHz max) running Windows 10.  They measure the time it takes to find a solution given a deck of cards, but do not include the time it takes to play the game.

#### Pyramid Board Challenges
Here are timings for clearing the board on 1500 random decks of cards.  502 of them have no possible way to clear the board and the timing measurement is how long the program takes to find out there's no solution.

| Decks                | Mean (ms) | Median (ms) | Maximum (ms) | Total (hrs:min:sec) |
|:-------------------- | ---------:| -----------:| ------------:| -------------------:|
| 1500 random decks    |       598 |         156 |        33093 |             0:14:56 |
| 998 solvable decks   |       296 |         157 |         4547 |             0:04:55 |
| 502 unsolvable decks |      1198 |          47 |        33093 |             0:10:01 |

#### Pyramid Score Challenges
Here are timings for finding out how to get the maximum possible score on the same 1500 random decks.

| Decks                | Mean (ms) | Median (ms) | Maximum (ms) | Total (hrs:min:sec) |
|:-------------------- | ---------:| -----------:| ------------:| -------------------:|
| 1500 random decks    |      6224 |        3804 |        76331 |             2:35:36 |

#### Pyramid Card Challenges
Here are timings for finding out how to remove as many Aces as possible from the game.  It will find out the best possible solution while clearing the board, and the best solution which does not clear the board, and compare them.

| Decks                | Mean (ms) | Median (ms) | Maximum (ms) | Total (hrs:min:sec) |
|:-------------------- | ---------:| -----------:| ------------:| -------------------:|
| 1500 random decks    |      6198 |        3610 |        67427 |             2:34:57 |

#### TriPeaks Board Challenges
Here are timings for clearing the board on the same 1500 random decks.  43 of them have no possible way to clear the board and the timing measurement is how long the program takes to find out there's no solution.

| Decks                | Mean (ms) | Median (ms) | Maximum (ms) | Total (hrs:min:sec) |
|:-------------------- | ---------:| -----------:| ------------:| -------------------:|
| 1500 random decks    |       111 |         109 |          329 |             0:02:46|
| 1457 solvable decks  |       111 |         109 |          329 |             0:02:42 |
| 43 unsolvable decks  |        81 |          93 |          125 |             0:00:04 |

#### TriPeaks Score Challenges
Here are timings for finding out how to get the maximum possible score on the same 1500 random decks.

| Decks                | Mean (ms) | Median (ms) | Maximum (ms) | Total (hrs:min:sec) |
|:-------------------- | ---------:| -----------:| ------------:| -------------------:|
| 1500 random decks    |       366 |         360 |          734 |             0:09:09 |

#### TriPeaks Card Challenges
Here are timings for finding out how to remove as many Aces as possible from the board on the same 1500 random decks.

| Decks                | Mean (ms) | Median (ms) | Maximum (ms) | Total (hrs:min:sec) |
|:-------------------- | ---------:| -----------:| ------------:| -------------------:|
| 1500 random decks    |       109 |         109 |          360 |             0:02:43 |

## Usage

### Command Line program

#### Requirements
- [64 bit JRE 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
- usually the program only needs a few hundred MB of RAM, but sometimes it needs a few GB
- Windows - untested as of yet on other operating systems

#### Steps for automatically playing Pyramid or TriPeaks Solitaire
1. Download the release and unzip it.
2. Go to solitaire-player/bin.
3. Start Microsoft Solitaire Collection on Windows 10.
4. In Microsoft Solitaire Collection start a Pyramid or TriPeaks Solitaire game - either a regular game or a challenge game.
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

The TriPeaks options are basically the same as Pyramid but with the word TriPeaks in place of Pyramid:
  - `solitaire-player.bat TriPeaks Board`
    - This runs the Board Challenge solver - clear the board in as few steps as possible.
  - `solitaire-player.bat TriPeaks Score`
    - This runs the Score Challenge solver - maximize the score in as few steps as possible.
  - `solitaire-player.bat TriPeaks Score <goal score> <current score>`
    - For example: "solitaire-player.bat TriPeaks Score 90000 44300" means your current score is 44300 and if you reach 90000 points you win.
    - This runs the Score Challenge solver but knows it can stop playing if it reaches the goal score.
  - `solitaire-player.bat TriPeaks Card <number of cards to remove> <card rank to remove> <current cards of that rank removed>`
    - Card ranks are one of the letters/numbers A 2 3 4 5 6 7 8 9 T J Q K.
    - For example: "solitaire-player.bat TriPeaks Card 10 K 4" means the goal is to remove 10 Kings, and you've removed 4 so far in previous games.
    - This runs the Card Challenge solver which tries to find the shortest way to remove a number of cards of a given rank.

#### Unknown Cards
In Pyramid Solitaire, we know the entire deck of cards because we can flip through the stock pile and see what they are, then restart the game.  But in TriPeaks, 18 of the cards are stuck face-down until we play the game and uncover them.  This program starts off by scanning through the cards in the stock pile and undoing the game, but then in TriPeaks, it will play to flip over face down cards until it knows enough of the deck to reach the goal or otherwise turn over all the cards.

It will start off showing the deck using ?? to represent unknown cards:
```
      ??          ??          ??
    ??  ??      ??  ??      ??  ??
  ??  ??  ??  ??  ??  ??  ??  ??  ??
7h  3d  7d  Ah  9h  5c  Td  Ac  2h  7c
8h
As 9d Ad 4s Jc 2d 7s Jh 4d Kd 9c 8d 5s 2c 4h Qd Ts 5d 4c Ks 6c Tc 3s
```

As it plays the game to reveal the unknown cards, keep verifying the new face up cards, but don't delete the ones that are removed from the game - it's just trying to figure out the entire deck before it restarts the game and plays the solution from the beginning.
    
#### Steps for solving a deck you give it
1. Create a file containing a deck of 52 cards that looks like this as an example.  Cards are two letters containing rank (A23456789TJQK) followed by suit (cdhs):

Pyramid Solitaire:
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

TriPeaks Solitaire:
```
      Jd          Qs          5h
    3c  6h      6s  8s      6d  Qc
  Js  Qh  Kc  Kh  9s  3h  2s  Th  8c
7h  3d  7d  Ah  9h  5c  Td  Ac  2h  7c
8h
As 9d Ad 4s Jc 2d 7s Jh 4d Kd 9c 8d 5s 2c 4h Qd Ts 5d 4c Ks 6c Tc 3s
```
In the example above, 8h is the cart initially at the top of the waste pile, As is the first face-down card at the top of the stock pile and 3s is the bottom.

2. Follow steps 1, 2, and 5 from the `Steps for automatically playing Pyramid or TriPeaks Solitaire` instructions, except in step 5, always add `-f <filename>` to give the program the deck of cards to solve.  It will skip any automation and just print out the solutions it finds.  For both Pyramid and TriPeaks Solitaire, the file must know all the cards.

### Source Code
This is a standard gradle project so you can run things like "gradlew distZip" and run the program using the distribution.
There is also a [Programming Guide](programming.md) with details for developers.
