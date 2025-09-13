# Draughts10x10
Java Swing International Draughts with AI

-white or black
-undo move
-hint moveable pieces
-rotate board
-AI search depth 1-7 (moves + value -> 1-8+)

Move animation.

Minimax with alfa beta pruning, uses bitboards. If depth is reached continues searching while board contains captures. 

All games are played on the same (static) squareboard and squares (hintBoard, pieceBoard, pieceMove).
