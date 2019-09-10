This isn't meant to be a full Python project, just some code and tests
for unoptimized Python versions of the solvers to compare against the Java
versions.

This code was written after someone opened an issue on GitHub that a TriPeaks
game was reported as impossible. So it would be useful to have a simple, slow
implementation to confirm that.

However, we do need to make some optimizations so that it can run in a
reasonable time and not require too much RAM.  TriPeaks isn't bad, just slow,
but Pyramid games can get really tough, so it was important to optimize the
state representation for size when we process 50+ million states to look for
the solution to one puzzle.