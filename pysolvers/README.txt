This isn't meant to be a full Python project, just some code and tests
for unoptimized Python versions of the solvers to compare against the Java
versions.

This code was written after someone opened an issue on GitHub that a game
was reported as impossible. So it would be useful to have a simple, slow
implementation to confirm that.

There is really only one optimization involved - instead of the State holding
the entire list of cards in the waste pile, it only holds a reference to the
card at the top of the waste pile. This makes a pretty large difference in
the speed of the program because there may be many ways to reach the same
state (tableau state, stock pile state, and top waste card) through different
paths.