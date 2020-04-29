package org.example.student.dotsboxgame

import uk.ac.bournemouth.ap.dotsandboxeslib.*
import uk.ac.bournemouth.ap.dotsandboxeslib.matrix.Matrix
import uk.ac.bournemouth.ap.dotsandboxeslib.matrix.MutableMatrix
import uk.ac.bournemouth.ap.dotsandboxeslib.matrix.MutableSparseMatrix
import uk.ac.bournemouth.ap.dotsandboxeslib.matrix.SparseMatrix
import java.lang.IllegalArgumentException
import kotlin.random.Random

class StudentDotsBoxGame(columns: Int, rows: Int, players: List<Player> = listOf(HumanPlayer(), HumanPlayer())) : AbstractDotsAndBoxesGame() {
    override val players: List<Player> = players.toList()

    var playerNumber: Int = 0

    override val currentPlayer: Player get() {
       return players[playerNumber]
    }

    // NOTE: you may want to me more specific in the box type if you use that type in your class
    override val boxes: Matrix<StudentBox> = MutableMatrix(columns, rows, ::StudentBox)

    val correctValue = fun(x: Int, y: Int): Boolean {
        if(y%2 == 0 && x == columns){
            return false
        }
        return true
    }

    override val lines: SparseMatrix<DotsAndBoxesGame.Line> = MutableSparseMatrix(columns + 1, (rows * 2) + 1, ::StudentLine, correctValue)

    override val isFinished: Boolean
        get() {
            for(box in boxes) {
                if(box.owningPlayer == null) {
                    return false
                }
            }
            return true
        }

    override fun playComputerTurns() {
        var current = currentPlayer
        while (current is ComputerPlayer && ! isFinished) {
            current.makeMove(this)
            current = currentPlayer
        }
    }

    inner class StudentLine(lineX: Int, lineY: Int) : AbstractLine(lineX, lineY) {
        override var isDrawn: Boolean = false


        override val adjacentBoxes: Pair<StudentBox?, StudentBox?>
            get() {
                var firstBox: StudentBox? = null
                var secondBox: StudentBox? = null
                if (lineY % 2 != 0) {
                    if (lineX != 0) {
                        firstBox = boxes[lineX - 1, (lineY - 1) / 2] //left
                    }
                    if (lineX < lines.maxWidth - 1) {
                        secondBox = boxes[lineX, (lineY - 1) / 2] //right
                    }

                } else {
                    if (lineY != 0) {
                        firstBox = boxes[lineX, (lineY / 2) - 1] //above
                    }
                    if (lineY < lines.maxHeight - 1) {
                        secondBox = boxes[lineX, lineY / 2] //below
                    }
                }
                return Pair(firstBox, secondBox)
            }

        override fun drawLine() {
            if (isDrawn) {
                throw IllegalArgumentException("Line is already taken. Try again.")
            } else {
                isDrawn = true

            }
            var takenBoxes = adjacentBoxes.toList()
            var completed = false
            for (box in takenBoxes) {
                if (box != null) {
                    var areLinesTaken = true
                    for (line in box.boundingLines) {
                        if (!line.isDrawn) {
                            areLinesTaken = false
                        }
                    }
                    if (areLinesTaken) {
                        box.owningPlayer = currentPlayer
                        completed = true
                    }
                }
            }
            if (completed) {
                if (isFinished) {
                    var scores = getScores()
                    var playerScores: MutableList<Pair<Player, Int>> = mutableListOf()
                    for (counter in 0 until players.size) {
                        playerScores.add(Pair(players[counter], scores[counter]))
                    }
                    fireGameOver(playerScores)
                }
            }
            else {
                playerNumber +=1
                if (playerNumber >= players.size) {
                    playerNumber = 0
                }
            }
            fireGameChange()
            playComputerTurns()
        }
    }
    inner class StudentBox(boxX: Int, boxY: Int) : AbstractBox(boxX, boxY) {

        override var owningPlayer: Player? = null

        override val boundingLines: Iterable<DotsAndBoxesGame.Line>
            get() {
                var lineMath : List<DotsAndBoxesGame.Line> = listOf(
                    lines[boxX, boxY*2], //line above
                    lines[boxX+1, (boxY*2)+1], //line right
                    lines[boxX, (boxY+1)*2], //line below
                    lines[boxX, (boxY*2)+1] //line left
                                                                   )
                return lineMath

            }

    }
}