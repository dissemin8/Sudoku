/* 
   Copyright 2018 Graeme Pinnock

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

//     _____           _       _          
//    / ____|         | |     | |         
//   | (___  _   _  __| | ___ | | ___   _ 
//    \___ \| | | |/ _` |/ _ \| |/ / | | |
//    ____) | |_| | (_| | (_) |   <| |_| |
//   |_____/ \__,_|\__,_|\___/|_|\_\\__,_|
//                                        

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;


/*
MAKE EACH CHECKSTEP count from 0 to 8.  Calc sx and sx from this count.
MAKE EACH CHECKSTEP count from 0 to 8.  Calc sx and sx from this count.
MAKE EACH CHECKSTEP count from 0 to 8.  Calc sx and sx from this count.
but make grid check 0 to 80

terminology

sqr =  a 1 by 1 square on the grid    
row =  a 9 by 1 horizontal bar on the grid made from 9 squares  
col =  a 1 by 9 vertical bar on the grid made from 9 squares 
box =  a 3 by 3 box on the grid made from squares)
grid = the 9 by 9 grid of 81 squares 

* rotate array so the entry grid displays right
* initialise the workGrid array

* drawGrid 
*  draw 81  1 by 1 outlines
*  draw  9  3 by 3 outlines
*  draw  1  9 by 9 outline
* 
* drawNumbers
*  draw the non blank numbers from the grid array
*  in the blank squares display a mini 3x3 grid of the current possibles numbers from the workgrid array   

*check each horizontal line to remove any numbers already correct for that line
*  hilite horizontal line being processed
*  hilite 1x1 box being processed
*  check workGrid candidate numbers in this line against each entry that is non zero the grid array 
*    remove any numbers that appear elsewhere in the same line in grid

*check each vertical line to remove any numbers already correct for that line
*  hilite vertical line being processed
*  hilite 1x1 box being processed
*  check workGrid candidate numbers in this line against each entry that is non zero the grid array 
*  remove any numbers that appear elsewhere in the same line in grid


*check each 3x3 box to remove any numbers already correct for that square
*  hilite 3x3 box being processed
*  hilite 1x1 square being processed
*  check workGrid candidate numbers in this box against each entry that is non zero the grid array 
*  remove any numbers that appear elsewhere in the same box in grid

*check each square in the 9x9 workgrid for single candidate squares
*  hilite 1x1 square being processed
*  check workGrid candidate numbers in this square so see if only one candidate remains
*    insert the single number in the relevant grid array location as candidate number + 10 

* check for lone candidate in row
* check for lone candidate in col    
* check for lone candidate in box   << Still to do
  
* check for "naked pairs"
*    these are 2 pairs of candidates in col / row / box     

* Optimise code to avoid checking rows / cols /boxes that are completely solved  << Still to do

* 1 Naked Singles
* 2 Hidden Singles
* 3 Naked Pairs
4 Point Pairs (or triples)
5 Claiming Pairs (or triples)
6 Naked Triples
7 X-Wings
8 Hidden Pairs
9 Naked Quads



data array formats

grid [0..8][0..8] contains the puzzle. 
  0      = square is unsolved                   (draw as blank to start)
  1..9   = solved square was supplied at start  (number drawn in black )
  11..19 = square solved by this program        (number drawn in blue  )
  
workgrid [0..8]  [0..8]    [0..9]
         gridx    gridy  candidatelist
* 0 = true (this square has been solved) false (there is more than one candidate for this square) 
  1 = true (1 is an candidate for this square) false (1 has been dismissed as a candidate)
  2 = true (2 is an candidate for this square) false (2 has been dismissed as a candidate)
  .
  .
  9 = true (9 is an candidate for this square) false (9 has been dismissed as a candidate)

NOTE: if contents of grid[x][y] is > 0, then the square is solved and 
      the contents of workGrid[x][y][1..9] should be ignored

*/

public class Sudoku {
	
	static int	debug = 1 ;
	
	public static final char[] GRAPHICS = { 0x2554, 0x2550, 0x2566, 0x2557, 
											0x2551, 0x2563, 0x255d, 0x2569, 
											0x255a, 0x2560, 0x256c, 0x2564,
											0x2562, 0x2567, 0x255f, 0x256a,
											0x253c, 0x256b, 0x2500, 0x2502} ;
	
	static final int [][][][] xyArray = {  // [ROW/COL/BOX][group (set of 9)][element (within group)][X|Y] 
		{
			{{0,0},{1,0},{2,0},{3,0},{4,0},{5,0},{6,0},{7,0},{8,0}},  // Row 0
			{{0,1},{1,1},{2,1},{3,1},{4,1},{5,1},{6,1},{7,1},{8,1}},  // Row 1
			{{0,2},{1,2},{2,2},{3,2},{4,2},{5,2},{6,2},{7,2},{8,2}},  // Row 2
			{{0,3},{1,3},{2,3},{3,3},{4,3},{5,3},{6,3},{7,3},{8,3}},  // Row 3
			{{0,4},{1,4},{2,4},{3,4},{4,4},{5,4},{6,4},{7,4},{8,4}},  // Row 4
			{{0,5},{1,5},{2,5},{3,5},{4,5},{5,5},{6,5},{7,5},{8,5}},  // Row 5
			{{0,6},{1,6},{2,6},{3,6},{4,6},{5,6},{6,6},{7,6},{8,6}},  // Row 6
			{{0,7},{1,7},{2,7},{3,7},{4,7},{5,7},{6,7},{7,7},{8,7}},  // Row 7
			{{0,8},{1,8},{2,8},{3,8},{4,8},{5,8},{6,8},{7,8},{8,8}}   // Row 8
		},{
			{{0,0},{0,1},{0,2},{0,3},{0,4},{0,5},{0,6},{0,7},{0,8}},  // Col 0
			{{1,0},{1,1},{1,2},{1,3},{1,4},{1,5},{1,6},{1,7},{1,8}},  // Col 1
			{{2,0},{2,1},{2,2},{2,3},{2,4},{2,5},{2,6},{2,7},{2,8}},  // Col 2
			{{3,0},{3,1},{3,2},{3,3},{3,4},{3,5},{3,6},{3,7},{3,8}},  // Col 3
			{{4,0},{4,1},{4,2},{4,3},{4,4},{4,5},{4,6},{4,7},{4,8}},  // Col 4
			{{5,0},{5,1},{5,2},{5,3},{5,4},{5,5},{5,6},{5,7},{5,8}},  // Col 5
			{{6,0},{6,1},{6,2},{6,3},{6,4},{6,5},{6,6},{6,7},{6,8}},  // Col 6
			{{7,0},{7,1},{7,2},{7,3},{7,4},{7,5},{7,6},{7,7},{7,8}},  // Col 7
			{{8,0},{8,1},{8,2},{8,3},{8,4},{8,5},{8,6},{8,7},{8,8}}   // Col 8
		},{
			{{0,0},{1,0},{2,0},{0,1},{1,1},{2,1},{0,2},{1,2},{2,2}},  // Box 0
			{{3,0},{4,0},{5,0},{3,1},{4,1},{5,1},{3,2},{4,2},{5,2}},  // Box 1
			{{6,0},{7,0},{8,0},{6,1},{7,1},{8,1},{6,2},{7,2},{8,2}},  // Box 2
			{{0,3},{1,3},{2,3},{0,4},{1,4},{2,4},{0,5},{1,5},{2,5}},  // Box 3
			{{3,3},{4,3},{5,3},{3,4},{4,4},{5,4},{3,5},{4,5},{5,5}},  // Box 4
			{{6,3},{7,3},{8,3},{6,4},{7,4},{8,4},{6,5},{7,5},{8,5}},  // Box 5
			{{0,6},{1,6},{2,6},{0,7},{1,7},{2,7},{0,8},{1,8},{2,8}},  // Box 6
			{{3,6},{4,6},{5,6},{3,7},{4,7},{5,7},{3,8},{4,8},{5,8}},  // Box 7
			{{6,6},{7,6},{8,6},{6,7},{7,7},{8,7},{6,8},{7,8},{8,8}}   // Box 8
		},

	} ;
	
	static String gridIn = "" ;
	
	static boolean	puzzleSolved	= false ;
	static boolean	unsolvable		= false ;
	static int		lastCellsSolved	= 0 ;

	static boolean [][] groupStatus = {
			{false,false,false,false,false,false,false,false,false},  // Row
			{false,false,false,false,false,false,false,false,false},  // Column
			{false,false,false,false,false,false,false,false,false}   // Box
	} ;
	
	static final int X = 0 ;
	static final int Y = 1 ;

	static final int ROW = 0 ;
	static final int COL = 1 ;
	static final int BOX = 2 ;
	
	static int [][] groupSolvedCount = new int [3][9]  ;
	
	static final String [] TYPES = {"Row","Col","Box"} ;
	
	static final String ver = "3.0" ;

	static int pass = 0 ;

	// Main loop  starts here
	
	static String [][] grid = new String[9][9] ; 
	
	public static void main(String[] args) {
		
		println(hhmmss() + " Soduku " + ver + " Started") ;
		
		// run the run-once tasks
		setup(args) ;

 		// keep running the solver until the puzzle is either solved, or we failed to solve
 		// any additional squares in a pass 
 		
 		while (!puzzleSolved && !unsolvable) {
 			
 			// testtest() ;
 			// if (debug > 0 ) return ;
 			
			clearScreen() ;
			drawNumbers() ;

			//checkRows() ;
			checkGroups(ROW) ;
			checkGroupsValid(ROW) ;

			clearScreen() ;
			drawNumbers() ;
						
			//checkRowsUniqCand() ;
			checkForHiddenSingles(ROW) ;
			checkGroupsValid(ROW) ;
			
			checkGroupsNakedPair(ROW) ;
			checkGroupsValid(ROW) ;
			
			clearScreen() ;
			drawNumbers() ;
			
			//checkCols() ;
			checkGroups(COL) ;
			checkGroupsValid(COL) ; 

			clearScreen() ;
			drawNumbers() ;
			
			//checkColsUniqCand() ;
			checkForHiddenSingles(COL) ;
			checkGroupsValid(COL) ; 

			checkGroupsNakedPair(COL) ;
			checkGroupsValid(COL) ;
			
			clearScreen() ;
			drawNumbers() ;

			// checkBoxes() ;
			checkGroups(BOX) ;
			checkGroupsValid(BOX) ;  
			
			clearScreen() ;
			drawNumbers() ;
			
			//checkBoxesUniqCand() ;
			checkForHiddenSingles(BOX) ;
			checkGroupsValid(BOX) ;
			
			checkGroupsNakedPair(BOX) ;
			checkGroupsValid(BOX) ;

			drawNumbers() ;
			checkForNakedSingles() ; // run more often ?

			clearScreen() ;
			drawNumbers() ;

			puzzleSoFar () ;  // add save puzzle ?

			checkPuzzle() ;
			
			checkPointingPairs(ROW) ;
			checkForNakedSingles() ;  
			
			checkPointingPairs(COL) ;
			checkForNakedSingles() ;  
			
			checkClaimingPairs(ROW) ;
			checkForNakedSingles() ;  
			
			checkClaimingPairs(COL) ;
			checkForNakedSingles() ;  
			
			pass++ ; 

		}
 		
		drawNumbers() ;
		println(hhmmss() + " Soduku " + ver + " Ended") ;
		// dumpg () ; // print out all the grid drawing graphics we know
		key() ; // print a key to the way the squares are printed  
	}
	
	static void setup (String[] args) {
		if (debug > 0) { println(hhmmss() + " setup Started ") ; }
		 
		println (hhmmss() + " The number or args supplied was " + args.length) ;
		if (args.length == 0 ) {
			println(hhmmss() + " I'll try to solve the built in example puzzle as one wasn't supplied as an argument.") ;
			// gridIn = "  9  6   3   7 2 6  84 5 3  319   6           4   398  2 7 14  4 7 5   9   6  7  " ;
			// gridIn = "  9  6   3   7 2 6  84 5 3  319   6           4   398  2 7 14  4 7 5   9   6  9  " ; // last 2 is 7
			gridIn = "82   47      72   7 6  3 42  7326   5 47892 3  2541 7  7 29 6     13  272 5467 81" ;
		} else if ((args.length == 1) && (args[0].length() == 81)) {
			println(hhmmss() + " I'll try to solve the puzzle supplied as an argument.") ;
			gridIn = args[0] ;
		} else {
			println(hhmmss() + " Puzzles must be supplied as one argument exactly 81 chars long.") ;
			if (args.length > 1) { 
				println("         I see too many args.") ;
			} 
			if (args[0].length() != 81) {
				println("         The argument supplied is " + args[0].length() + " characters.") ;
			}
			return ;
		}
		 
 		stringGridToGrid() ;  // new grid loader

 		if (debug > 0) { println(hhmmss() + " setup Ended ") ; }
	}
	
	static String getCellStr (int type, int group, int cell) {
		return grid [xyArray [type][group][cell][X]] [xyArray [type][group][cell][Y]] ;
	}

	static void setCellStr (int type, int group, int cell, String cellStr) {
		// if we just have one candidate left, shift it left 1 position to mark cell as solved
		if ( cellStr.length() == 3 ) {
			println(" Solved " + TYPES[type] + " " + group + " cell " + cell + " as " + cellStr.substring(2));
			cellStr = cellStr.substring(1) ;
		}
		grid [xyArray [type][group][cell][X]] [xyArray [type][group][cell][Y]] = cellStr ;
	}
	
	static boolean cellSolved (int type, int group, int cell) {
		return getCellStr(type, group, cell).length() < 3 ;
	}

	static boolean getGroupStatus (int type, int group) {
		return (groupStatus[type][group]) ;
	}

	static void setGroupStatus (int type, int group, boolean state) {
		groupStatus[type][group] = state  ;
	}
	
	public static final void   print(Object x) { System.out.print(x)  ; }
	
	public static final void println(Object x) { System.out.println(x); }
	
	public static final boolean findCand(String needleStr, String haystackStr) {
		// String haystackStr = getCellStr (type, group, cell) ;
		if (haystackStr.indexOf(needleStr) > -1) { 
			return true ;
		} else {
			return false ;
		}
	}

	public static final void removeCand(int type, int group, int cell, String needleStr) {
		String haystackStr = getCellStr (type, group, cell) ;
		if (haystackStr.length() > 2 ) {
			if (haystackStr.indexOf(needleStr) > 1) { 
				setCellStr(type, group, cell, haystackStr.replace(needleStr,"")) ;
			}
		} else {
			println ("ERROR **************** trying to remove cand from solved square ") ; 
		}
	}

	static void checkClaimingPairs(int type) {

		// Claiming pairs/triples
		// when one or more candidates appear in a line within a box,
		// and those candidates do not appear elsewhere in the line,
		// those candidates cannot appear anywhere on the rest of the box and can be removed
		// Only a valid check for type = ROW / COL 
		// 
		//  1.1 yyy yyy      yyy 11. yyy      ... ... xxx   etc      
		//  xxx ... ...      ... xxx ...      yyy yyy .11
		//  xxx ... ...      ... xxx ...      ... ... xxx
		//
		//  if "1" only appears where shown and not in any "y" cell
		//  then "1" can be removed from all "x" squares

		if (debug > 0) { println(hhmmss() + " checkPointingPairs Started") ; }
		// process rows or columns, boxes are n/a 
		println("type(" + TYPES[type] + ")") ;
		for (int box = 0 ; box <9 ; box++) {    // process each of the 9 boxes
			println("  box(" + box + ") boxstatus(" + getGroupStatus (BOX, box) + ")") ;
			if (!getGroupStatus (BOX, box)) { // skip solved boxes
				// if in ROW mode calculate number of first ROW for this box
				int lLineStart = 0 ;
				int bLineStart = 0 ;
				int chipStart = 0 ;
				if (type == ROW) { 
					lLineStart = (int) (Math.floor(box/3.0) * 3) ;
					bLineStart = box % 3 * 3 ;
					chipStart  = box % 3 * 3 ;
				}
				// if in COL mode calculate number of first COL for this box
				if (type == COL) {
					lLineStart = box % 3 * 3 ;
					bLineStart = (int) (Math.floor(box/3.0) * 3) ;
					chipStart  = (int) (Math.floor(box/3.0) * 3) ;  
				}
				// int lineEnd = lLineStart + 2 ;  
				// process each of the 3 lines crossing this box 
				for (int line = lLineStart ; line <= (lLineStart + 2) ; line++) {
					println("    line(" + line + ") Llinestart(" + lLineStart + ") chipStart(" + chipStart + ") linestatus(" + getGroupStatus (type, line) + ")") ;
					if (!getGroupStatus (type, line)) { // skip completed lines
						// get the source "chips" (the 3x1) for this line
						int cell1 = chipStart ; // int cell1 = sourceChip * 3 ;
						int solvedCount = 0 ;
						if (cellSolved (type, line, cell1)) solvedCount++ ;
						if (cellSolved (type, line, cell1+1)) solvedCount++ ;
						if (cellSolved (type, line, cell1+2)) solvedCount++ ;
						if (solvedCount < 2) { // at least 2 cells must be unsolved  
							String sCandsStr = "" ;
							String s1 = getCellStr(type, line, cell1) ;
							if (s1.length() > 2) sCandsStr += s1.substring(2) ;
							String s2 = getCellStr(type, line, cell1 + 1) ;
							if (s2.length() > 2) sCandsStr += s2.substring(2) ;
							String s3 = getCellStr(type, line, cell1 + 2) ;
							if (s3.length() > 2) sCandsStr += s3.substring(2) ;
							// get the target chips (the 3x1) for this line
							if (sCandsStr.length() > 1) { // source chip must have > 1 chars to possibly have a pair
								String pairStr = "" ;
								for (int cand = 1 ; cand <= 9 ; cand++) {
									String candStr = String.valueOf(cand) ;
									// find first occurrence of cand in string
									int i1 = sCandsStr.indexOf(candStr) ;
									// is this cand actually in sCandsStr ?
									if (i1 > -1) {
										// last occurrence of cand in string
										int i2 = sCandsStr.lastIndexOf(candStr) ; 
										// if the first and last are not in the same place, there is more than 1!
										if (i2 != i1) {          
											pairStr = pairStr + candStr ;
										}
									}
								}
								// At this point we might have a pair
								if (pairStr.length() > 0) { // only scan other chips if we have a potential pointing pair
									println("     pairStr(" + pairStr + ")") ;
									// *----------------------------------------------------*
									// | go through each candidate in pairStr and then      |
									// | findMatch will only have one needle.               |
									// | Then, if pairStr isn't found in the rest-of-line   |
									// | we can safely remove if from the rest of the BOX   |
									// | without checking anything else (we may not find it |
									// | though, but it doesn't matter                      |
									// *----------------------------------------------------*
									// go through each of the pair (triple) candidate and 
									// see if they are in the rest of the line 
									for (int pIndex = 0 ; pIndex < pairStr.length() ; pIndex++ ) {
										String pairCharStr = pairStr.substring(pIndex,pIndex+1) ;
										boolean lineFind = false ;
										// go through targets elsewhere in the LINE 
										for (int lTargetChip = 0 ; lTargetChip < 3 ; lTargetChip ++) {
											if (chipStart != (lTargetChip * 3)) { // don't check against self
												cell1 = lTargetChip * 3 ;
												solvedCount = 0 ; 
												// Check each cell to see if it is solved
												if (cellSolved (type, line, cell1)) solvedCount++ ;
												if (cellSolved (type, line, cell1+1)) solvedCount++ ;
												if (cellSolved (type, line, cell1+2)) solvedCount++ ;
												if (solvedCount < 3) { // at least 1 cell must be unsolved
													String ltCandsStr = "" ;
													String t1 = getCellStr(type, line, cell1) ;
													if (t1.length() > 2) ltCandsStr += t1.substring(2) ;
													String t2 = getCellStr(type, line, cell1 + 1) ;
													if (t2.length() > 2) ltCandsStr += t2.substring(2) ;
													String t3 = getCellStr(type, line, cell1 + 2) ;
													if (t3.length() > 2) ltCandsStr += t3.substring(2) ;
													println("      Source(" + sCandsStr + ") line(" + line + ") ltCandsStr(" + ltCandsStr + ") pairStr(" + pairStr + ")" ) ;
													// check to see if any chip contains same numbers as pairStr
													if (findCand(pairCharStr, ltCandsStr)) lineFind = true ;
												}
											}
										}
	                                    // If no pairCharStr in the rest of the line,
										// it is safe to remove pairCharStr from the rest of the box
										// go through targets elsewhere in the BOX
										if (lineFind == false) {
											println("      potential line claiming pair/triple - removing box candidates") ;
											for (int bLine = lLineStart ; bLine < (lLineStart + 3) ; bLine ++) {
												if (line != bLine ) { // don't check against self
													cell1 = bLineStart  ;  
													solvedCount = 0 ;
													// Check each cell to see if it is solved
													if (!cellSolved (type, bLine, cell1)) {
														removeCand(type, bLine, cell1, pairCharStr) ;
													}
													if (!cellSolved (type, bLine, cell1+1)) {
														removeCand(type, bLine, cell1+1, pairCharStr) ; 
													}
													if (!cellSolved (type, bLine, cell1+2)) {
														removeCand(type, bLine, cell1+2, pairCharStr) ;
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}           
		if (debug > 0) { println(hhmmss() + " checkPointingPairs Ended") ; }
	}

	static void checkPointingPairs(int type) {
		// Pointing pairs/triples
		// when one or more candidates appear in a line within a box,
		// and those candidates do not appear elsewhere in the box,
		// those candidates cannot appear anywhere on the rest of the line and can be removed
		// Only a valid check for type = ROW / COL 
		// 
		//  1.1 yyy yyy      yyy 11. yyy      ... ... xxx   etc      
		//  xxx ... ...      ... xxx ...      yyy yyy .11
		//  xxx ... ...      ... xxx ...      ... ... xxx
		//
		//  if "1" only appears where shown and not in any "x" cell
		//  then "1" can be removed from all "y" squares
		//
		//  aaa bbb ...      aaa ... bbb      bbb aaa ...      ... aaa bbb      bbb ... aaa      ... bbb aaa           
		//
		if (debug > 0) { println(hhmmss() + " checkPointingPairs Started") ; }
		// process rows or columns, boxes are n/a 
		println("type(" + TYPES[type] + ")") ;
		for (int box = 0 ; box <9 ; box++) {    // process each of the 9 boxes
			println("  box(" + box + ") boxstatus(" + getGroupStatus (BOX, box) + ")") ;
			if (!getGroupStatus (BOX, box)) { // skip solved boxes
				// if in ROW mode calculate number of first ROW for this box
				int lLineStart = 0 ;
				int bLineStart = 0 ;
				int chipStart = 0 ;
				if (type == ROW) { 
					lLineStart = (int) (Math.floor(box/3.0) * 3) ;
					bLineStart = box % 3 * 3 ;
					chipStart  = box % 3 * 3 ;
				}
				// if in COL mode calculate number of first COL for this box
				if (type == COL) {
					lLineStart = box % 3 * 3 ;
					bLineStart = (int) (Math.floor(box/3.0) * 3) ;
					chipStart  = (int) (Math.floor(box/3.0) * 3) ; // TODO - is this really right ?
				}
				// int lineEnd = lLineStart + 2 ;  
				// process each of the 3 lines crossing this box 
				for (int line = lLineStart ; line <= (lLineStart + 2) ; line++) {
					println("    line(" + line + ") Llinestart(" + lLineStart + ") chipStart(" + chipStart + ") linestatus(" + getGroupStatus (type, line) + ")") ;
					if (!getGroupStatus (type, line)) { // skip completed lines
						// get the source "chips" (the 3x1) for this line
						int cell1 = chipStart ; // int cell1 = sourceChip * 3 ;
						int solvedCount = 0 ;
						if (cellSolved (type, line, cell1)) solvedCount++ ;
						if (cellSolved (type, line, cell1+1)) solvedCount++ ;
						if (cellSolved (type, line, cell1+2)) solvedCount++ ;
						if (solvedCount < 2) { // at least 2 cells must be unsolved  
							String sCandsStr = "" ;
							String s1 = getCellStr(type, line, cell1) ;
							if (s1.length() > 2) sCandsStr += s1.substring(2) ;
							String s2 = getCellStr(type, line, cell1 + 1) ;
							if (s2.length() > 2) sCandsStr += s2.substring(2) ;
							String s3 = getCellStr(type, line, cell1 + 2) ;
							if (s3.length() > 2) sCandsStr += s3.substring(2) ;
							// get the target chips (the 3x1) for this line
							if (sCandsStr.length() > 1) { // source chip must have > 1 chars to possibly have a pair
								String pairStr = "" ;
								for (int cand = 1 ; cand <= 9 ; cand++) {
									String candStr = String.valueOf(cand) ;
									// find first occurrence of cand in string
									int i1 = sCandsStr.indexOf(candStr) ;
									// is this cand actually in sCandsStr ?
									if (i1 > -1) {
										// last occurrence of cand in string
										int i2 = sCandsStr.lastIndexOf(candStr) ; 
										// if the first and last are not in the same place, there is more than 1!
										if (i2 != i1) {          
											pairStr = pairStr + candStr ;
										}
									}
								}
								// At this point we might have a pair
								if (pairStr.length() > 0) { // only scan other chips if we have a potential pointing pair
									println("     pairStr(" + pairStr + ")") ;
									for (int pIndex = 0 ; pIndex < pairStr.length() ; pIndex++ ) {
										String pairCharStr = pairStr.substring(pIndex,pIndex+1) ;
										boolean  boxFind = false ;
										// go through targets elsewhere in the BOX
										for (int bLine = lLineStart ; bLine < (lLineStart + 3) ; bLine ++) {
											if (line != bLine ) { // don't check against self
												cell1 = bLineStart  ;  
												solvedCount = 0 ;
												// Check each cell to see if it is solved
												if (cellSolved (type, bLine, cell1)) solvedCount++ ;
												if (cellSolved (type, bLine, cell1+1)) solvedCount++ ;
												if (cellSolved (type, bLine, cell1+2)) solvedCount++ ;
												if (solvedCount < 3) { // at least 1 cell must be unsolved
													String btCandsStr = "" ;
													String t1 = getCellStr(type, bLine, cell1) ;
													if (t1.length() > 2) btCandsStr += t1.substring(2) ;
													String t2 = getCellStr(type, bLine, cell1 + 1) ;
													if (t2.length() > 2) btCandsStr += t2.substring(2) ;
													String t3 = getCellStr(type, bLine, cell1 + 2) ;
													if (t3.length() > 2) btCandsStr += t3.substring(2) ;
													println("      Source(" + sCandsStr + ") bLine(" + bLine + ") btCandsStr(" + btCandsStr + ") pairStr(" + pairStr + ")") ;
													// check to see if any chip contains same numbers as pairStr
													if (findCand(pairCharStr, btCandsStr)) boxFind = true ;
												}
											}
										}
										// go through targets elsewhere in the LINE
										if (boxFind == false) {
											for (int lTargetChip = 0 ; lTargetChip < 3 ; lTargetChip ++) {
												if (chipStart != (lTargetChip * 3)) { // don't check against self
													cell1 = lTargetChip * 3 ;
													// Check each cell to see if it is solved
													if (!cellSolved (type, line, cell1)) {
														removeCand(type, line, cell1, pairCharStr) ;
													}
													if (!cellSolved (type, line, cell1+1)) {
														removeCand(type, line, cell1+1, pairCharStr) ;
													}
													if (!cellSolved (type, line, cell1+2)) {
														removeCand(type, line, cell1+2, pairCharStr) ;
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}           
		if (debug > 0) { println(hhmmss() + " checkPointingPairs Ended") ; }
	}
	
	static void puzzleSoFar () {
		println("<---0---><---1---><---2---><---3---><---4---><---5---><---6---><---7---><---8--->") ;
		for (int row = 0 ; row < 9 ; row++ ) {
			for (int cell = 0 ; cell < 9 ; cell++ ) {
				if (cellSolved (ROW, row, cell)) {
					print(getCellStr(ROW, row, cell).replaceAll(" ","")) ;
				} else {
					print(" ") ;
				}
			}	
		}
		println("") ;
	}
	
	static void dumpg () {
		// print all the grid graphics
		int ci = 0 ;
		for (char c : GRAPHICS) {
			print(ci++) ;
			System.out.printf(" %s , ", new String(Character.toChars(c)));
		}
		println("") ;
	}
	
	static void wait(int secs) {
		// wait for the specified number of seconds
		try {
			TimeUnit.SECONDS.sleep(secs);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	static void g(int gchar, int count) {
		// print a graphics symbol the specified number of times
		// (Cannot be displayed by the eclipse console) 
		for (int i = 0 ; i < count ; i++) {
			System.out.printf("%s", new String(Character.toChars(GRAPHICS[gchar])));
		}
	}
	
	public static void clearScreen() {
		// clear the terminal window (doesn't work in the eclipse console)
		if (debug < 1) {
			try {
				new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static String hhmmss() {
		// returns time as a string in the form hh:mm:ss  
	    return 	String.format("%02d", LocalDateTime.now().getHour()) + ":" +
	    		String.format("%02d", LocalDateTime.now().getMinute()) + ":" +
	    		String.format("%02d", LocalDateTime.now().getSecond()) 
	    		;
	}

	static void stringGridToGrid() {
		// read in string grid and convert to integer array
		if (debug > 0) { println(hhmmss() + " stringGridToNewGrid Started") ; }
		for (int col = 0 ; col < 9 ; col++) {
			for (int row = 0 ; row < 9 ; row++) {
				int pos = ( col ) + ( row * 9 )  ; 
				String cell = gridIn.substring(pos,pos+1) ;
				if (cell.equals(" ")) cell = "  123456789" ; 
				grid [col][row] = cell ;  
			}
		}
		if (debug > 0) { println(hhmmss() + " stringGridToNewGrid Ended") ; }
	}

	static void drawNumbers() {
		if (debug > 0) { println(hhmmss() + " drawNumbers Started") ; }
		println(hhmmss() + " This is pass number " + pass ) ;
		// Draw to border
		g(0,1) ; g(1,29) ; g(2,1) ; g(1,29) ; g(2,1) ;g(1,29) ; g(3,1) ; println("") ;
		//
		for (int gridy = 0 ; gridy < 9 ; gridy++) {
			for (int celly = 0 ; celly < 3 ; celly++) {
				g(4,1) ; // double vertical bar 
				for (int gridx = 0 ; gridx < 9 ; gridx++) {
					String cellEntry = getCellStr (ROW, gridy, gridx) ;
					for (int cellx = 0 ; cellx < 3 ; cellx++) {
						if (cellEntry.length() == 2) {  // " 1"  = we solved this cell as a 1
							// we solved this cell
							String number = cellEntry.substring(1) ;
							String num = "" ;
							switch (cellx) {
								case 0 :
									num = "  " + number   ;
									break ;
								case 1 :
									num = "-" + number + "-" ;
									break ;
								case 2 :
									num =  number + "  " ;
									break ;
							}
							print(num) ;
						} else if (cellEntry.length() == 1) { // "1 "  = "1" was given in the supplied puzzle
							// this cell was already filled in
							String num = "" ;
							switch (cellx) {
								case 0 :
									num = "  " + cellEntry ;  
									break ;
								case 1 :
									num = cellEntry + cellEntry + cellEntry ;
									break ;
								case 2 :
									num = cellEntry + "  " ;  
									break ;
							}
							print(num) ;
						} else {  // cellEntry.length() > 2 
							// this cell is unsolved so list candidates  "  123456789" 
							String candList = cellEntry.substring(2) ;  // the list of remaining candidates
							String candidate = String.format("%1d", celly * 3 + cellx + 1 )  ;     // will be a number from 1 to 9
							if (candList.indexOf(candidate) >= 0) {
								String num = " " + candidate + " " ;
								print(num) ;
							} else {
								print("   ") ;
							}
						}
					}
					if ((gridx + 1) % 3 == 0) {
						g(4,1) ;
					} else {
						g(19,1) ;
					}
				}
				println("") ;
			}
			//println("") ;
			if (gridy == 8) {
				g(8,1) ; g(1,29) ; g(7,1) ; g(1,29) ; g(7,1) ;g(1,29) ; g(6,1) ; println("") ;
			} else if ((gridy + 1) % 3 == 0) {
				g(9,1) ; g(1,29) ; g(10,1) ; g(1,29) ; g(10,1) ;g(1,29) ; g(5,1) ; println("") ;
			} else {
				g(4,1) ; g(18,9) ; g(16,1) ; g(18,9) ; g(16,1) ;  g(18,9) ; g(4,1) ; g(18,9) ; g(16,1) ; g(18,9) ; g(16,1) ;  g(18,9) ; g(4,1) ; g(18,9) ; g(16,1) ; g(18,9) ; g(16,1) ;  g(18,9) ; g(4,1) ; println("") ;   
			}
		}
		if (debug > 0) println(hhmmss() + " drawNumbers Ended") ; 
		// wait(1) ;
	}
	
	static void key () {
		println("key to grid print") ;
		g(16,1) ; g(18,9) ; g(16,1) ;                       print("                      "); g(16,1) ; g(18,9) ; g(16,1) ;                       print("               "); g(16,1) ; g(18,9) ; g(16,1) ;                       println("") ;
		g(19,1) ; print("  11111  ") ; g(19,1) ; print(" Initially filled     "); g(19,1) ; print("  1-1-1  ") ; g(19,1) ; print(" Solved        "); g(19,1) ; print(" 1       ") ; g(19,1) ; println(" Remaining       ");
		g(19,1) ; print("  11111  ") ; g(19,1) ; print(" in at start          "); g(19,1) ; print("  1-1-1  ") ; g(19,1) ; print(" by this       "); g(19,1) ; print("    5    ") ; g(19,1) ; println(" candidates      ");
		g(19,1) ; print("  11111  ") ; g(19,1) ; print(" of puzzle            "); g(19,1) ; print("  1-1-1  ") ; g(19,1) ; print(" programme     "); g(19,1) ; print(" 7  8  9 ") ; g(19,1) ; println(" for this square ");
		g(16,1) ; g(18,9) ; g(16,1) ;                       print("                      "); g(16,1) ; g(18,9) ; g(16,1);                        print("               "); g(16,1) ; g(18,9) ; g(16,1) ;                       println("") ;
	}

	static int statusChecks () {
		if (debug > 0) println(hhmmss() + " statusChecks Started") ; 
		int totalSolvedCount = 0 ;
	    for (int type = 0 ; type < 3 ; type++ ) {
			for (int group = 0 ; group < 9 ; group++) {
				groupSolvedCount[type][group] = 0 ;
			    if (!getGroupStatus (type, group)) {    
			    	for (int cell = 0 ; cell < 9 ; cell++) {
			    		if (cellSolved(type, group, cell)) {   
			    			groupSolvedCount[type][group]++ ;
			    			if (type == ROW) totalSolvedCount++ ; 
			    			if ( groupSolvedCount[type][group] == 9 ) {
			    				setGroupStatus (type, group, true) ;
			    				println(hhmmss() + " " + TYPES[type] + " " + group + " complete") ;
			    			}
			    		}
			    	}
			    } else {
			    	if (type == ROW) totalSolvedCount += 9 ; 
			    }
		    }
		}
		if (debug > 0) println(hhmmss() + " statusChecks Ended") ;
		return totalSolvedCount ;
	}

	static void checkGroups(int type) {  
		if (debug > 0) println(hhmmss() + " checkGroups " + TYPES[type] + " Started") ; 
		for (int group = 0 ; group < 9 ; group++) {
	    	if (!getGroupStatus(type,group)) { // only check this group if it is not flagged as solved 
	    		for (int cell1 = 0 ; cell1 < 9 ; cell1++) {
	    			if (!cellSolved(type, group, cell1)) { // only check this square if it is unsolved
	    				//   [cell1][group]  is the location of the unsolved cell
	    				//   [cell2][group]  is the location of the potentially solved cell
	    				for (int cell2 = 0 ; cell2 < 9 ; cell2 ++) {
	    					// remove candidates when other square is solved as the same number  
	    					if (cellSolved(type, group, cell2)) {  // only check when cells has been solved 
	    						if (cell1 != cell2) {   // don't check against self
	    							// we only want the number that is in pos 1 *OR* 2
	    							String solved = getCellStr(type, group, cell2).replaceAll(" ","") ;
	    							// get the cell we want to update 
	    							String cellToUpdate = getCellStr(type, group, cell1) ;
	    							// remove the solved number from the candidate list and write it back 
	    							setCellStr(type, group, cell1, cellToUpdate.replace(solved,"")) ;
	    		    			}
	    					}
	    				}
	    			}
	    		}
	    	}
	    }
	    if (debug > 0) println(hhmmss() + " checkGroups " + TYPES[type] + " Ended") ;
	}

	static void checkForHiddenSingles(int type) {
		if (debug > 0)  println(hhmmss() + " checkForHiddenSingles " + TYPES[type] + " Started") ; 
		for (int group = 0 ; group < 9 ; group++) {
			if (!getGroupStatus(type, group)) { // only check this group if it is not flagged as solved 
				for (int cell1 = 0 ; cell1 < 9 ; cell1++) {
					if (!cellSolved(type, group, cell1 )) { //  only check this cell if it is unsolved
						// [cell1][group]  is the location of the source cell  
						// [cell2][group]  is the location of the target cell  
						for (int cand = 1 ; cand < 10 ; cand++) {
							String candStr = Integer.toString(cand) ;
							int matchCount = 0 ;
							boolean valid = false ;  
							for (int cell2 = 0 ; cell2 < 9 ; cell2++ ) {
								if ( !cellSolved(type, group, cell2 ) ) { // check only unsolved squares
									if (cell1 != cell2) {    // don't check self against self
										// only check if this candidate set in source square
										if (getCellStr(type, group, cell1).indexOf(candStr) >= 0 ) {
											valid = true ;
											// if (workGrid[x][y][cand] && workGrid[cell2][y][cand]) {
											if (getCellStr(type, group, cell2).indexOf(candStr) >= 0) {
												matchCount++ ; // not unique!
											}
										}
									}
								}
							}
							if ((matchCount == 0) && valid) {
								// this candidate was unique so set sell to solved-by-computer
								setCellStr(type, group, cell1, " " + candStr) ;
							}
						}
					}
				}
	    	}
    	}
		if (debug > 0)  println(hhmmss() + " checkForHiddenSingles " + TYPES[type] + " Ended") ;
	}

	static void checkGroupsNakedPair(int type) {
		if (debug > 0)  println(hhmmss() + " checkGroupsNakedPair " + TYPES[type] + " Started") ;		
		for (int group = 0 ; group < 9 ; group++) {
			if (!getGroupStatus(type, group)) { // only check this group if it is not flagged as solved
				// [cell1][group]  is the location of the source cell  
				// [cell2][group]  is the location of the target cell  
				for (int cell1 = 0 ; cell1 <= 7 ; cell1++) { // cell 1 is in range 0..7
					//  only check this cell if it has exactly 2 candidates remaining
					String cell1Str = getCellStr(type, group, cell1) ;
					if (cell1Str.length() == 4) { 
						// cell s in in range cell1+1 .. 8
						for (int cell2 = cell1 + 1 ; cell2 <= 8 ; cell2++ ) { 
							// only check target cells with exactly 2 candidates remaining
							String cell2Str = getCellStr(type, group, cell2) ;
							// if the naked match, they must be a naked pair 
							if ( cell1Str.equals(cell2Str) ) {  
								// remove naked pair candidates from
								// all other cells in this group
								println("  Found Naked Pair in " + TYPES[type] + " " + group + " cells " + cell1 + " and " + cell2) ;
								for (int cell3 = 0 ; cell3 <= 8 ; cell3++ ) {
									// ignore solved squares and cells 1 and 2 
									if ( 	(cell3 != cell1) 	&& 
											(cell3 != cell2) 	&& 
											(!cellSolved (type, group, cell3) ) ) {
										String cell3Str = getCellStr(type, group, cell3) ;
										String naked1 = cell2Str.substring(2,3) ;
										String naked2 = cell2Str.substring(3,4) ;
										if (debug > 1) println(" removing " + naked1 + " and " + naked2 + " from " + cell3Str) ;
										cell3Str = cell3Str.replaceAll(naked1,"") ;
										cell3Str = cell3Str.replaceAll(naked2,"") ;
										setCellStr (type, group, cell3, cell3Str) ;
									}
								}
							}
						}
					}
				}
	    	}
    	}
		if (debug > 0)  println(hhmmss() + " checkGroupsNakedPair " + TYPES[type] + " Ended") ;
	}
	static void checkForNakedSingles() {
		if (debug > 0)  println(hhmmss() + " checkForNakedSingles Started") ;  
		int type = ROW ; 
		// all 81 squares - it only needs to be done as ROW as it will cover all cells
		for (int group = 0 ; group < 9 ; group++) {
			for (int cell = 0 ; cell < 9 ; cell++) {
				if ( getCellStr(type, group, cell).length() == 3 ) {
					if (debug > 1) {
						print(getCellStr(type, group, cell) + " ") ;
					}
					// move last remaining candidate one position left 
					// into the solved-by-computer position 
					setCellStr(type, group, cell, getCellStr(type, group, cell).substring(1))  ;
					if (debug > 1) {
						println(" ncs: square " + group + " , " + cell + " solved") ;
					}
				} else {
					if (debug > 1) print("... ") ;
				}
			}
			if (debug > 1) println("") ;
		}
		if (debug > 0)  println(hhmmss() + " checkForNakedSingles Ended") ;  
	}

	static boolean checkPuzzle() {  
		if (debug > 0) println(hhmmss() + " checkPuzzle Started") ;  
		int cellsSolved = statusChecks () ;
		if (debug > 1) {
    		print(hhmmss() + " lastCellsSolved(" + lastCellsSolved + ") cellsSolved (" + cellsSolved);
			print(") solvedthispass (") ; println(cellsSolved - lastCellsSolved + ")" );
		}
		if (lastCellsSolved == cellsSolved) {
			unsolvable = true ;
			print(hhmmss() + " **** puzzle seems unsolvable ****");
			if (debug > 0) println(hhmmss() + " checkPuzzle Ended") ;
			return true ;
		}
		lastCellsSolved = cellsSolved ; 
		if ( cellsSolved == 81) { 
			puzzleSolved = true ;
			println(hhmmss() + " puzzle solved");
		} else {
			int perc = (int)(cellsSolved / 81.0 * 100) ;
			println(" Solved " + cellsSolved + " out of 81 (" + perc + " Percent)") ;
		}
		if (debug > 0) println(hhmmss() + " checkPuzzle Ended") ;
		return puzzleSolved ;
	}	

	static void checkGroupsValid(int type) {  
		// check that no two solved squares are the same number in every group (Row / Col / Box)
		if (debug > 0) println(hhmmss() + " checkGroupsValid " + TYPES[type] + " check Started") ; 
		// 0 = Row, 1 = Col, 2 = Box
		for (int group = 0 ; group < 9 ; group++) { // rows
	    	for (int cell1 = 0 ; cell1 < 8 ; cell1++) { // columns
	    		for (int cell2 = cell1 + 1 ; cell2 < 9 ; cell2++) {
		    		// [cell1][group]  is the location of the source square
		    		// [cell2][group]  is the location of the target square  
		    		String cell1Str = grid[cell1][group] ; // first cell for compare
	    			String cell2Str = grid[cell2][group] ; // second cell for compare
	    			if ( cellSolved(type, group, cell1 ) && cellSolved(type, group, cell2 ) ) {
	    				//	(cell1Str.length() < 3) & (cell2Str.length() < 3) ) { // only check solved cells
	    				cell1Str = getCellStr(type, group, cell1).replaceAll(" ","") ;
	    				cell2Str = getCellStr(type, group, cell2).replaceAll(" ","") ;
	    				if ( cell1Str.equals(cell2Str) ) { // error condition detected
	    					unsolvable = true ;
	    					print(hhmmss()) ; 
	    					print(" *error* Identical solved squares detected in " + TYPES[type] + " " + group) ;
	    					println(" cell " + cell1 + " and " + cell2 + " are both " + cell1Str ) ;
	    				}	
	    			}
	    		}
	    	}
		}
		if (debug > 0) println(hhmmss() + " checkGroupsValid " + TYPES[type] + " check Ended") ;
	}
}