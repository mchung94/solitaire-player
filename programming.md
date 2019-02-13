# Programming Guide
The purpose of this guide is to provide an introduction to help developers
understand the code quickly.

## Background
This program was created because I wanted to figure out the solution to
Pyramid Solitaire games when I got stuck playing them in Microsoft Solitaire
Collection on Windows 10.  More recently, I have added support for TriPeaks
Solitaire.

[pyramid-solver](https://github.com/mchung94/pyramid-solver) is an earlier
project I created to investigate how to solve Pyramid Solitaire games as
quickly as possible given a deck of cards.  But there were two problems with
putting it into practical use when I'm stuck on a game I'm playing:

1. Creating a text file with all the cards in the game to pass into the program
   is tedious.
2. pyramid-solver only knows how to remove the 28 pyramid cards.  However,
   Microsoft Solitaire Collection also has Score and Card challenges which have
   different goals.  For example, "Earn a score of 2,400", or "Clear 12 Aces in
   2 deals".

The solution I chose was to create a new Java program using the following:

1. [SikuliX](http://sikulix.com/) which lets us programmatically scan what's
   on the screen and automate mouse clicks, among other things.  With this, the
   program can identify all the cards on its own, and even just play the game
   outright.
2. [JNA](https://github.com/java-native-access/jna) lets us easily call Win32
   APIs to find and resize the Microsoft Solitaire Collection window to a
   consistent size.  This makes using SikuliX easier.  

## Package and Class Structure
The main three categories of packages and classes are:
1. Windows
2. Solvers
3. Players

Window classes: `MSCWindow` and its subclasses `PyramidWindow` and
`TriPeaksWindow` are meant to represent the Microsoft Solitaire Collection
window and interact with it, such as looking at cards in the game and guessing
what they are, as well as clicking on cards or buttons.

Solver classes: The `com.secondthorn.solitaireplayer.solvers` package contains
classes meant to take a deck of cards and find a solution to the solitaire
game.  One design principle is to keep each game's solvers completely separate,
so the pyramid package for solving Pyramid Solitaire shares no code with the
tripeaks package for solving TriPeaks Solitaire.

Players: `SolitairePlayer` and its subclasses `PyramidPlayer` and
`TriPeaksPlayer` use the Window classes and Solver classes and guide the user
from the beginning of the game to the end by figuring out what to do using the
solver classes, then interacting with the Microsoft Solitaire Collection window
to perform the actions the solvers give as the solution.

## SikuliX Usage
This section contains information on how and why SikuliX is used the way it is
in this program.

### What SikuliX is used for
- To scan the Microsoft Solitaire Collection window for the placement and order
  of all the cards in the game
- To click on cards and buttons in the game window to play the game
- To interact with the user using popups (to verify the cards, and to choose a
  solution to play if there are multiple solutions)

### The Microsoft Solitaire Collection Window
The game's internal image of the table and cards is very large, but then it
is scaled down to fit into the Microsoft Solitaire Collection window.

In `com.secondthorn.solitaireplayer.players.MSCWindow`, solitaire-player uses
Win32 functions to activate the window, move it to the upper left corner,
resize it to 1024x768, and move it to the foreground.  This happens at several
points during the program runtime in case some activity has happened in
between.  This helps the rest of the code make some assumptions about the size
and location of cards on screen.

SikuliX sometimes guesses cards incorrectly because the images are so small.
So the program will ask the user to verify any cards it scans to correct any
mistakes.

### Key Ideas for SikuliX Usage
To deal with these issues, there are different images in the resource directory
for each of the three scaling sizes.

There are also JSON files containing the locations of where each card in the
the game should be at.

For each card rank and suit, there is a subdirectory for containing multiple
images of each rank/suit when necessary due to the differences based on
location on-screen.

When searching for the Draw button, solitaire-player just searches for the
image, but when trying to figure out what card is at a given location, it
looks for the best matching rank and suit in each card region then asks the
user to verify it afterwards.

### Waiting
If the player clicks on things too soon after clicking on something else, it
might not register in the Microsoft Solitaire Collection program.  The biggest
wait times are for recycling the waste pile and undoing the board to restart a
game.

SikuliX has pretty good support for waiting for things, like waiting for an
image to appear or vanish.  But it's not always obvious what to wait for here
so solitaire-player just calls sleep() after clicking on things for simplicity.

### Playing Solutions
solitaire-player knows that SikuliX can't always guess the cards accurately.
It will ask the user to verify and edit the cards if it knows something's wrong
with its guess (missing/duplicate cards for example).

This means that when playing a solution, we can't tell the program to just
find the Eight of Spades and remove it, because the card recognition might have
failed earlier and it might try to click on the Three of Spades.  So the
solution also contains a reference to the location for SikuliX to click on,
based on the verified list of cards it was given.

## Solitaire Solver Algorithm Discussion
It's not too complicated to create a Breadth-First Search or A\* search to
solve these games, but there needs to be some major work on optimization or it
will take a long time and use too much memory.  I've increased the speed of
the Pyramid Solver by over 50x to make sure it can run in a reasonable time on
a 8GB laptop.

There's two main insights in how to improve performance on these search
algorithms:
1. State Representation: Representing the state of the game by using lists or
arrays of cards, no matter how the cards are represented, is too slow.  This
makes checking if a state has already been seen earlier in the search take too
long.  For Pyramid and TriPeaks Solitaire, we can represent the state of the
game using a single 64-bit long (for Pyramid) or 32-bit int (for TriPeaks).
The optimizations are discussed later in this document.
2. Pyramid and TriPeaks both use 28 cards on the "tableau" or board.  Both
have the rule that a card can't be removed unless the cards blocking it from
below are removed first.  So for a given deck of cards, there are only 1430
possible configurations of cards remaining on the board for Pyramid Solitaire,
and 22,932 for TriPeaks.  This gives us a huge opportunity for precalculating
a lot of things for Pyramid Solitaire, enough so that the majority of time in
the search process is just bit twiddling and hash table lookups.  TriPeaks
doesn't need this however.  It's pretty fast without doing this - I tried it
anyway but it wasn't worth it.

### Card Representation
Almost every time I see playing cards represented in code, it's as a class
using enums for ranks and suits.  And decks of cards end up being a class
containing a list or array of cards.

When people optimize cards, they tend to represent a card using an integer or
some bit representation.  One easy optimization is to represent a card as an
integer from 0 to 51 inclusive and treat them as IDs - any data you'd like to
know about the card can be encoded as arrays using the card ID as a lookup
index.

I spent a lot of time exploring ways to optimize card representation and
realized for Pyramid and TriPeaks, it's unnecessary once we optimize the state
representation.

So in this code, there's no card class - cards are just two letter strings
consisting of a rank character (A23456789TJQK) followed by a suit character
(cdhs).  Unknown cards in TriPeaks are represented by the string "??".  This is
to emphasize that they're just pieces of data we look up when we need, and that
they aren't involved in much real work.

### State Representation
Pyramid and TriPeaks are different from other games like Klondike, Spider, or
FreeCells because we don't build stacks of cards and move them around.  Both
games have 28 cards on the board that just sit there until they're either
removed from the game entirely (in Pyramid) or moved to the top of the waste
pile (in TriPeaks).  They both have stock and waste piles too.

The simple way to represent game state would be to have a State class that
stores:
- An array of 28 cards for the board.
- A list of cards in the stock pile.
- A list of cards in the waste pile.
- Maybe some miscellaneous information like the current score or streak in
TriPeaks.

The trick to storing the entire game state in a single 32-bit or 64-bit value
is to store the minimum possible information that is needed to derive the
full state given the deck of cards and the path of states from the beginning
of the game to the current state which are stored separately.  In other words,
make the state refer to the deck of cards instead of contain the deck of cards.

In Pyramid Solitaire we could represent the game with a State class using:
- 52 bits to indicate which of the 52 cards of the deck remain in the game.
- An integer from 28 to 52 indicating which card in the deck of cards is
currently the top card of the stock pile.  Any cards with deck index higher
than this is the rest of the stock pile, and any cards with deck index below
this not including the 28 pyramid cards is the waste pile.  52 means the stock
pile is empty.
- The number of times the waste pile has been recycled, since the player can
recycle the waste pile up to two times.

But we can use 52 bits for the flags, 6 bits for an integer from 28 to 52 for
the stock card index, and two bits for the number of times the waste pile was
recycled.  That's 60 bits for the entire representation which we can pack into
a 64-bit long.

The Common Lisp version of the Pyramid Solitaire solver goes a bit further, by
encoding the first 28 of the 52 bits as a number from 0 to 1429 (11 bits)
because there's only 1430 possible values of the first 28 bits.  There's a few
optimizations that follow from that but they're not necesary here.

In TriPeaks Solitaire, we use:
- 15 bits to store an integer from 0 to 22931 as an ID representing each valid
configuration of 28 cards on the tableau.
- 6 bits for the deck index of the card on top of the waste pile (0 to 51).
- 6 bits for the deck index of the card on top of the stock pile (29 to 52
where 52 means the stock pile is empty).

With states being represented as unboxed ints or longs, we use the
[Trove library](http://trove.starlight-systems.com/) for its collections
on unboxed primitives.  I tried to see what happens if I switch to another
library for collections but Trove still seems to be the fastest for my
purposes.

### TriPeaks Solitaire Algorithms and Data Structures
Let's discuss TriPeaks first because it's a lot simpler.

There are four solvers for TriPeaks, all of which use Breadth-First Search, and
always return a single best list of steps to play.
1. Board Challenge Solver: find the shortest path to clearing the board.
2. Score Challenge Solver: find the shortest path to get the goal score if
possible, or try to find the maximum possible score if not (it's not guaranteed
to find the maximum possible score).
3. Card Challenge Solver: find the shortest path to the goal of removing a
number of cards of a certain rank from the tableau.  If the goal can't be
reached, try to find the shortest path to clearing the tableau.  If that can't
be done either, just try to clear the most of the goal card rank as possible.
4. Card Revealing Solver: find the shortest path to turn over a face down card
that isn't known yet.  Iterate and keep turning over face down cards, undoing
the board and starting from the beginning if necessary.

The main loop in `TriPeaksPlayer.autoplay()` tries to solve the game, and
for as long as the solution indicates that there might be a better solution if
we reveal more of the the unknown face-down cards, it will run the card
revealing solver.

Because of the card revealing process, Solutions returned by the solver classes
have a method called `isDefinitiveSolution()`.  A solution is a definitive
solution when the solver determines that turning over more face down cards
and knowing what they are can't give us a better solution, for example when
the player can already reach a score goal without knowing any more cards.

### Suboptimal Score Challenge Solutions
Score Challenges for TriPeaks don't guarantee the best possible solution.  The
reason is because it's possible to arrive at the same state in different ways
with a different score and/or streak.

One way to reach the state might have a higher score but lower streak than
another way, but we can't tell which is better unless we search both ways to
see if the lower scoring path overtakes the other.

So currently, we just let the one with the better current score win and skip
over paths with lower score but longer streak.  That's not right, but it takes
an extremely long time to try both.

### Score and Solution Steps calculation
The Breadth-First Search process uses a mapping from state to previous state
which can be used to look up the steps from the beginning of the game to the
current state.  But calculating the score uses a `ScoreCache` mapping from a
state to a score and streak value so calculating the score for a state needs
just one lookup to the previous state's score instead of going all the way back
to the beginning of the game to calculate the score.

### FIFO Queue
The `IntFIFOQueue` class is a first-in, first-out queue for unboxed ints.
Trove doesn't have this.  The implementation is similar to ArrayDeque which
uses a common technique for queues - creating a power-of-two sized array to
hold the data, with indexes pointing to the head and tail which can wrap
around the ends of the underlying array.

## Pyramid Solitaire Solver Algorithms and Data Structures
This section discusses the Pyramid Solitaire solving algorithm.

There are three solvers for Pyramid Solitaire, which return a mapping from a
solution description string to a list of `Actions` to perform.
- Board Challenge Solver - A\* algorithm with unwinnable state detection.  The
  goal state is when all 28 pyramid cards have been removed, so we can skip all
  states that are found to be unwinnable (impossible to remove one of the cards
  on the pyramid).
- Score Challenge Solver - Breadth-First Search, exiting early if the goal
  score is reached, otherwise doing an exhaustive search for the maximum
  possible score.  The solution with the fewest moves is returned.
- Card Challenge Solver - Breadth-First Search, exiting early if the goal
  number of cards has been removed, otherwise doing an exhaustive search for
  the best result while clearing the board and the best result while not
  clearing the board.  There's a test case that shows an example where the
  player can either clear the board, removing 2 Fours, or get stuck before
  clearing the board, but removing 3 Fours.  The program can't decide the
  best solution so it returns both and asks the user to pick one.

### Heuristic Function
In the Board Challenge Solver, for each step while playing the game, the
heuristic function calculates an estimate of how many more steps are needed to
win the game.  The following calculation is
[admissible](https://en.wikipedia.org/wiki/Admissible_heuristic) and
[consistent](https://en.wikipedia.org/wiki/Consistent_heuristic):
1. Count how many cards of each rank are on the table.
2. Find the higher count of every matching rank pair.  For example, if there
   are two Sixes and three Sevens on the table, the higher count is three, so
   it would take a minimum of three steps to remove all the Sixes and all the
    evens.  Find the higher count for A/Q, 2/J, 3/T, 4/9, 5/8, and 6/7.
3. Calculate the sum of the number of kings plus each of the six counts in the
   previous step.  This is the estimated number of steps to win the game.
   
### Unwinnable State Detection
The actual code is faster with some precalculation, but the overall process to
find out if a state is unwinnable is:
```
for each card on the pyramid that isn't a King:
    find all the cards with the rank that adds up to 13 as a potential match
    filter out the ones that are covering or covered by the card
    if none are left, there's no way to remove the card, so it's unwinnable
```

### State Representation
A state shows where all the cards are and which cycle through the stock cards
we are on at each step of playing the game.

We just want to store the minimum amount of information in each state - only
the things that can change from state to state.  We don't need to store
sequences of cards in each state.  Instead, focus on storing flags and indexes,
and create a mapping from index to card somewhere else when we need to look it
up.

States are represented using a 60-bit value stored in a long:
- Bits 0-51: bit flags to indicate which cards in the 52 card deck have been
  removed so far (called deck flags in the code)
  - Bits 0-27 are the 28 pyramid cards (called pyramid flags in the code)
  - Bits 28-51 are the 24 stock/waste cards
- Bits 52-57: a 6-bit integer from 28 to 52 referring to the index of the card
  at the top of the waste pile (called stock index in the code)
  - the cards with index higher than this are the rest of the stock
  - the cards with index lower than this are the waste pile, the closest
    remaining card below this index is the top of the waste pile (called
    waste index in the code)
  - if the stock index is 52, the stock is empty, and if the waste index is 27,
    the waste pile is empty
- Bits 58-59: a 2-bit integer from 1 to 3 indicating which cycle through the
  stock cards we are currently in

The 52 bit flags map to this arrangement:
```
            00
          01  02
        03  04  05
      06  07  08  09
    10  11  12  13  14
  15  16  17  18  19  20
21  22  23  24  25  26  27

stock/waste cards: 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50 51
```

### Precalculations
Because there are only 1430 possible values for bits 0-27 of a state (the
pyramid cards), we can take a deck of cards and quickly precalculate all
possible values for some information we need for each state.

### Precalculations that are not specific to a deck of cards
The following information is known before the program runs and is stored in
the `com.secondthorn.solitaireplayer.solvers.pyramid.Pyramid` class.
- `Pyramid.COVER_MASKS` - Hardcoded masks for each pyramid card, indicating
  which pyramid card indexes cover it from below.  The tests show how to
  calculate it.  These masks are used to check if a pyramid card is uncovered.
- `Pyramid.UNRELATED_CARD_MASKS` - Hardcoded masks for each pyramid card, with
  0 set on the pyramid card indexes which are covering or covered by the card.
  The tests show how to calculate it.  These masks are used to exclude cards
  which can't be removed with the pyramid card because they are blocking each
  other.
- `Pyramid.ALL` contains 1430 instances of the Pyramid class created from the
  pyramid data loaded from resources/pyramid/Pyramid.json:
  - `Pyramid.flags` - The 24 bits of pyramid flags.
  - `Pyramid.uncoveredIndexes` - For the given pyramid flags, an array of the 
    uncovered pyramid cards' indexes.
  - `Pyramid.allIndexes` - For the given pyramid flags, an array of all the
    remaining pyramid cards' indexes.

### Precalculations that happen for each deck of cards
These are in the `com.secondthorn.solitaireplayer.solvers.pyramid.Deck` class.
One useful tip to understand what's going on here is to imagine that the cards
themselves are not the fundamental objects in the game - it's actually the deck
of 52 cards and indexes into it that the solver thinks with. 
- `Deck.values` - An array of each card's numeric value.  This can be
  used to check if a card index refers to a king, or if two card indexes add up
  to 13 and can be removed together.
- `Deck.cardRankMasks` - An array indexed by card rank value (1 - 13), holding
  a mask with bits set for each card in the deck with that value.  So index 1
  would contain a mask showing the position of each Ace in the deck.

The four functions we need from a state are assisted by the following
precalculations which are stored in
`com.secondthorn.solitaireplayer.solvers.pyramid.StateCache` objects.  There
are 1430 `StateCache` objects, one for each value of the 24 bit pyramid flags.
1. Check if the state is a goal state
   - Precalculation: only the pyramid flags value of 0 is a goal state.
   - At runtime: look up the value for the given state.
2. Check if a state is unwinnable
   - Precalculation: for each remaining pyramid card that isn't a king, collect
     masks that check if there exists a card that can remove the pyramid card.
   - At runtime: to check if a state is unwinnable, mask off the bits from the
     state using each mask, and if the result is zero, that means there is a
     card can't be removed from the pyramid.
3. Calculate the heuristic function on the state
   - Precalculation: there's only 1430 pyramid flag values, so there's only
     1430 possible values for the heuristic function, calculated using
     `Pyramid.allIndexes` and `Deck.values`.
   - At runtime: just look up the value for the given state.
4. Calculate successor states for each possible action from the state
   - Precalculation: for each value of `Pyramid.uncoveredIndexes`, generate a
     2D array indexed by stock index and waste index.  For each combination of
     all 3 values, look for all combinations of cards that can be removed and
     generate a list of card removal masks.
   - At runtime: when we generate successor states for each state, we have to
     handle drawing a card and recycling the waste pile, but the successor
     state for every other action we can take is created by removing the cards
     indicated by each mask and then updating the stock index if necessary.

### Search Node Representation
In general, nodes for search algorithms like Breadth-First Search or A\* have
the following fields:
- State: the state represented by the search node
- Parent Node: the node with the previous state
- Action: the action taken from the previous state to reach this state
- Depth: the number of nodes from the initial state's node to this node

For memory usage improvements,
`com.secondthorn.solitaireplayer.solvers.pyramid` has the classes `Node` and
`NodeWithDepth` to represent linked lists of states starting with the current
state and going back to the initial state (so the same parent node is shared by
each successor state's node).  But also as a speed optimization, NodeWithDepth
contains the depth as well as the node it's for.  So the fields can be
generated like this:
- State: `NodeWithDepth.node.state`
- Parent State: `NodeWithDepth.node.parent.state`
- Action: Derive this by diffing the state and parent state with a logical
  bitwise XOR, and see what changed.
  - If the cycle changed (bits 58-59), then the waste pile was recycled.
  - If any bits from 0-51 changed, the cards at those indexes were removed.
  - Otherwise, the stock index changed without removing any cards, so the
    action was drawing a card from the stock to the waste pile.
- Depth: `NodeWithDepth.depth`

### Priority Queue Implementation
A general purpose
[priority queue](https://en.wikipedia.org/wiki/Priority_queue) is unnecessary.
A [bucket queue](https://en.wikipedia.org/wiki/Bucket_queue) is very fast and
makes sense for solving Pyramid Solitaire, since we only need to insert nodes,
remove the minimum priority node, and check if the queue is empty.  The bucket
queue is implemented at
`com.secondthorn.solitaireplayer.solvers.pyramid.BucketQueue`.

#### Minimum and Maximum Solution Lengths
In order to create the bucket queue with the correct number of buckets, we need
to know at least the maximum possible solution length - this would be the
highest valued bucket in the bucket queue.

The shortest possible solution to Pyramid Solitaire is 15 steps.  This happens
if each step removes two table cards, except the last two which can't be
removed together since one covers the other.  13 pairs of table cards + 2 more
for the last two cards.

The longest possible solution to Pyramid Solitaire would be 102 steps:
1. Draw 24 times (24 steps so far)
2. Recycle the waste pile (25 steps so far)
3. Draw 24 more times (49 steps so far)
4. Recycle the waste pile again (50 steps so far)
5. Draw 24 more times (74 steps so far)
6. Remove 24 pyramid cards by pairing with a card on the waste pile, which has
   no Kings.  The stock and waste piles are now empty. (98 steps so far)
7. Remove the last 4 pyramid cards, which are Kings (102 steps).

I don't think there exists a deck where that would be the shortest possible
solution, but in any case, a bucket queue with buckets 0 to 102 would work for
our priority queue.
