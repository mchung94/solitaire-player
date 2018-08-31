# Programming Guide
The purpose of this guide is to provide an introduction to help developers
understand the code quickly.

## Background
This program was created because I wanted to figure out the solution to
Pyramid Solitaire games when I got stuck playing them in Microsoft Solitaire
Collection on Windows 10.

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

SikuliX sometimes guesses cards incorrectly when the window is so small, so
if the window is maximized on a 1920x1080 monitor and there's a normal sized
taskbar, the program will play the game without resizing it.

But there are still some things to be aware of when using SikuliX on the
Microsoft Solitaire Collection window:
- Even though the window should be 1024x768, the Windows Display Settings
  scaling size can change this:
  - 100% - the window size becomes 1024x768
  - 200% - the window size becomes 1026x977
  - 250% - the window size becomes 1282x1221
- In goal challenges (as opposed to regular games), the goal is displayed in
  a bar on the bottom of the game window.  This used to squash the rest of the
  cards into a smaller space, and the program had to adjust its expectations of
  the cards' size and location.  But as of the April 2018 Windows 10 update,
  this is no longer the case - the cards look the same and are in the same
  positions of the screen regardless of whether or not the goal bar is there.
- The cards themselves look slightly different not only based on the window
  size, but also their positions on screen.  If the top of the pyramid is a
  three of clubs, the 3 would look slightly different than if it was at the
  bottom of the pyramid.  This is because of the algorithm used to squash a
  large image into a smaller window.

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

## Solver
This section discusses the Pyramid Solitaire solving algorithm.

### Memory Usage
States are just long values, so for reduced memory usage, we use the
[Trove library](http://trove.starlight-systems.com/) for its collections
on unboxed primitives.  

### Algorithms
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

### Key Ideas
1. The solver needs to avoid revisiting states by keeping track of visited
   states, otherwise it may take a really long time regardless of anything else
   we do.  For example Depth-First Search sometimes finds a non-optimal
   solution in just a few milliseconds, but there are always decks where it
   could run for hours unless it avoids revisiting states.
2. From initial performance testing, I learned that representing the state of
   the game using sequences of cards (such as vectors/lists/strings), no matter
   what card representation I used, was too slow.
3. There are only 1430 valid arrangements of cards in the pyramid.  The rule is
   that a card can't be removed if there are still cards covering it from
   below.  This is small enough that it is feasible to precalculate things for
   every possible pyramid card arrangement given a deck of cards.
4. Cards don't really need to move in Pyramid Solitaire, unlike other games
   like FreeCell.  Cards in the pyramid never move, they just get removed.  And
   if the 24 stock/waste cards were in one array, just an index into the array
   pointing to the top card of the stock pile is sufficient to simulate all the
   card moves.  If all the cards with higher index are the rest of the stock
   pile, and all the cards with lower index are the waste pile, then:
   - Incrementing the index draws a card from the stock to the waste pile.
   - Resetting the index back to the first card is recycling the waste pile.

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
