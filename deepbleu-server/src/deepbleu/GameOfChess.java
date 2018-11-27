package deepbleu;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.concurrent.Callable;

import com.google.gson.Gson;

/** ___ _____   @@@@@@@   @@@@@@@@  @@@@@@@@  @@@@@@@   @@@@@@@   @@@       @@@@@@@@  @@@  @@@
 * /\ (_)    \  @@@@@@@@  @@@@@@@@  @@@@@@@@  @@@@@@@@  @@@@@@@@  @@@       @@@@@@@@  @@@  @@@
  /  \      (_, @@!  @@@  @@!       @@!       @@!  @@@  @@!  @@@  @@!       @@!       @@!  @@@
 _)  _\   _    \!@!  @!@  !@!       !@!       !@!  @!@  !@   @!@  !@!       !@!       !@!  @!@
/   (_)\_( )____\!@  !@!  @!!!:!    @!!!:!    @!@@!@!   @!@!@!@   @!!       @!!!:!    @!@  !@!
\_     /    _  _/@!  !!!  !!!!!:    !!!!!:    !!@!!!    !!!@!!!!  !!!       !!!!!:    !@!  !!!
 *) /\/  _ (o)( !!:  !!!  !!:       !!:       !!:       !!:  !!!  !!:       !!:       !!:  !!!
 *\ \_) (o)   / :!:  !:!  :!:       :!:       :!:       :!:  !:!   :!:      :!:       :!:  !:!
 * \/________/   :::: ::   :: ::::   :: ::::   ::        :: ::::   :: ::::   :: ::::  ::::: ::
 * 
 * @author Matthew Tosques
 * 
 *  TO DO:
 *      - Regarding Chess: 
 *          * En Passant, Castling, Pawn Promotion, 50 Move Rule, Threefold Repetition.
 *      - Regarding Back-end: 
 *          * Find bugs.
 *          
 */

public class GameOfChess implements Callable<EndGameState> {
	
	Board BOARD;
	
	Gson gson = new Gson();
	
	public GameOfChess(Player playerOne, Player playerTwo) {
		BOARD = new Board(playerOne, playerTwo);
	}

    /*
    //static final ImageView CHECK_ICON = new ImageView(new Image("img/checkText.png"));
    //static final TextArea MOVE_HISTORY = new TextArea(playerOne + " vs " + playerTwo + "\n"
    //        + new Date().toString() + "\n");
     * 
     */
	
	@Override
	public EndGameState call() throws Exception {
		Player winner = this.getWinner();
		boolean wasDraw = winner instanceof ConsolePlayer && winner.name.equals("DRAW");
		EndGameState theEnd = new EndGameState(BOARD.player1, BOARD.player2, winner, wasDraw);
		return theEnd;
	}

    /*
    public static void main(String[] args) {
    	System.out.println("The winner is: " + GameOfChess.getWinner());
    }
    /*

    /**
     * Allows legal moves until checkmate or draw. Returns the winning player.
     */
    Player getWinner() {
        while (!(BOARD.hasDraw() || BOARD.kingCaptured())) {
            HashSet<ChessMove> allLegalMoves = BOARD.getAllLegalMoves();
            if (allLegalMoves.isEmpty()) {
                return this.BOARD.getWinner();
            }
            if (BOARD.hasCheck()) { //if current player is in check
                //CHECK_ICON.setOpacity(100); //display check icon in the toolbar
                boolean canExitCheck = false;
                for (ChessMove legalMove : allLegalMoves) { //see if any move gets current player to safety
                    BOARD.move(legalMove); //simulate move
                    BOARD.switchTurns();
                    if (!canExitCheck) {
                        canExitCheck = !BOARD.hasCheck();
                    }
                    BOARD.switchTurns();
                    BOARD.undoLastMove();
                }
                if (!canExitCheck) { //if all moves leave player in check the game is over
                    return BOARD.getWinner();
                }
            } else {
                //CHECK_ICON.setOpacity(0);
            }
            ChessMove mostRecentMove = playValidMove();
            //BOARD.updateGraphics();
            
            //check for network players and send move as json
            if(BOARD.currentPlayer instanceof NetworkPlayer) {
            	NetworkPlayer otherGuy = (NetworkPlayer) BOARD.currentPlayer;
                try {
					BufferedWriter buffOut = new BufferedWriter(
							new OutputStreamWriter( otherGuy.getSocket().getOutputStream() ) );
					String moveJson = gson.toJson(mostRecentMove);
					System.out.println("Writing ChessMove json to network...");
					buffOut.write(moveJson);
					buffOut.newLine();
					buffOut.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}

            }
            
            
        }
        return BOARD.getWinner();
    }

    /**
     * Makes sure moves make sense before we send them to the board.
     */
    ChessMove playValidMove() {
        boolean valid = false;
        while (!valid) {
            System.out.println(BOARD);
            System.out.println(this.BOARD.currentPlayer + "'s turn.  Total number of legal moves: "
                    + BOARD.getAllLegalMoves().size());
            ChessMove potentialMove = BOARD.currentPlayer.getMove(BOARD);
            if (!(BOARD.currentPlayer instanceof ConsolePlayer)
                    || BOARD.isLegalMove(potentialMove)) {
                System.out.print("Final decision: " + BOARD.currentPlayer + " moved " + potentialMove + ".  \n");
                for (Piece[] row : BOARD.tiles) {
                    for (Piece p : row) {
                        if (p != null && p.x == potentialMove.toRow && p.y == potentialMove.toCol) {
                            System.out.println("MOVE CAPTURED PIECE: " + p.getClass().getSimpleName());
                        }
                    }
                }
                System.out.println("\n");
                //MOVE_HISTORY.appendText(BOARD.currentPlayer + " moved " + potentialMove + "\n");
                BOARD.move(potentialMove);
                valid = true;
                return potentialMove;
            } else {
                System.out.println(BOARD);
                System.out.println("Invalid move.");
            }
        }
        return null;
    }



    
    /**
     * configure UI -> display board -> start game -> declare winner
     *
     * @throws java.lang.Exception
     */
    /*
    @Override
    public void start(Stage primaryStage) throws Exception {

        //Board
        BorderPane root = new BorderPane();
        root.setCenter(BOARD);

        //Right region
        Pane rightArea = new VBox();
        rightArea.setMaxWidth(225);
        root.setRight(rightArea);

        //Big logo
        ImageView bigLogo = new ImageView(new Image("/img/icon.png"));
        bigLogo.setOpacity(0.25);
        bigLogo.setFitWidth(bigLogo.getImage().getWidth() / 3);
        bigLogo.setFitHeight(bigLogo.getImage().getHeight() / 3);
        bigLogo.setTranslateX(bigLogo.getImage().getWidth() / 3);
        bigLogo.setTranslateY(24);

        //configure move history text area
        MOVE_HISTORY.setEditable(false);
        MOVE_HISTORY.setMinHeight(360);

        //Undo one move button
        Button undoOneMove = new Button("Undo One Move");
        undoOneMove.setAlignment(Pos.CENTER);
        undoOneMove.setMinWidth(rightArea.getMaxWidth() + 1);
        undoOneMove.setOnAction((event) -> {
            if (!BOARD.moveHistory.isEmpty()) {
                BOARD.undoLastMove();
                int count = 0;
                while (count++ <= 1) {
                    MOVE_HISTORY.setText(MOVE_HISTORY.getText()
                            .substring(0, MOVE_HISTORY.getText().lastIndexOf("\n")));
                }
                MOVE_HISTORY.appendText("\n");
            }

            BOARD.selected = null;

            if (BOARD.hasCheck()) {
                CHECK_ICON.setOpacity(1);
            } else {
                CHECK_ICON.setOpacity(0);
            }
            BOARD.updateGraphics();
        });
        undoOneMove.setDisable(true);

        //Undo two moves button
        Button undoTwoMoves = new Button("Undo Two Moves");
        undoTwoMoves.setAlignment(Pos.CENTER);
        undoTwoMoves.setMinWidth(rightArea.getMaxWidth() + 1);
        undoTwoMoves.setDisable(true);
        undoTwoMoves.setOnAction((event) -> {
            if (BOARD.moveHistory.size() > 1) {
                BOARD.undoLastMove();
                BOARD.undoLastMove();
                int count = 0;
                while (count++ <= 2) {
                    MOVE_HISTORY.setText(MOVE_HISTORY.getText()
                            .substring(0, MOVE_HISTORY.getText().lastIndexOf("\n")));
                }
                MOVE_HISTORY.appendText("\n");
            }
            BOARD.selected = null;
            BOARD.updateGraphics();
            if (BOARD.hasCheck()) {
                CHECK_ICON.setOpacity(1);
            } else {
                CHECK_ICON.setOpacity(0);
            }
        });

        //Player type selection menus
        HBox playerSelectLabels = new HBox();
        Label p1Label = new Label("\nConfigure White");
        p1Label.setDisable(true);
        p1Label.setMinWidth(rightArea.getMaxWidth() / 2 - 1);
        p1Label.setAlignment(Pos.CENTER);
        Label p2Label = new Label("\nConfigure Black");
        p2Label.setDisable(true);
        p2Label.setMinWidth(rightArea.getMaxWidth() / 2);
        p2Label.setAlignment(Pos.CENTER);

        playerSelectLabels.getChildren().addAll(p1Label, p2Label);

        HBox doubleDropDown = new HBox();
        ComboBox blackSelect = new ComboBox();
        blackSelect.getItems().addAll("Human", "AI");
        blackSelect.setValue("AI");
        blackSelect.setMinWidth(rightArea.getMaxWidth() / 2);
        blackSelect.setDisable(true);

        ComboBox whiteSelect = new ComboBox();
        whiteSelect.getItems().addAll("Human", "AI");
        whiteSelect.setValue("Human");
        whiteSelect.setMinWidth(rightArea.getMaxWidth() / 2);
        whiteSelect.setDisable(true);

        HBox playerNameFields = new HBox();
        TextField whiteName = new TextField(playerOne.name);
        whiteName.setAlignment(Pos.CENTER);
        whiteName.setDisable(true);
        whiteName.setMinWidth(rightArea.getMaxWidth() / 2);
        TextField blackName = new TextField(playerTwo.name);
        blackName.setDisable(true);
        blackName.setAlignment(Pos.CENTER);
        blackName.setMinWidth(rightArea.getMaxWidth() / 2);
        
        playerNameFields.getChildren().addAll(whiteName, blackName);

        Button updatePlayers = new Button("Update Players");
        updatePlayers.setAlignment(Pos.CENTER);
        updatePlayers.setMinWidth(rightArea.getMaxWidth()+1);
        updatePlayers.setOnAction((event) -> {
            Player oldPlayerOne = playerOne;
            Player oldPlayerTwo = playerTwo;
            if(whiteName.getText().equals(blackName.getText())) {
                if(whiteName.getText().length() > 0) {
                    whiteName.setText(whiteName.getText() + " White");
                    blackName.setText(blackName.getText() + " Black");
                }
                else {
                    whiteName.setText("White");
                    blackName.setText("Black");
                }
            }
            boolean playerOneUp = playerOne == BOARD.currentPlayer;
            if(whiteSelect.getSelectionModel().getSelectedItem().toString().equals("AI")) {
                playerOne = new ComputerPlayer(whiteName.getText(), true);
            } else playerOne = new GUIPlayer(whiteName.getText(), true);    
            if(blackSelect.getSelectionModel().getSelectedItem().toString().equals("AI")) {
                playerTwo = new ComputerPlayer(blackName.getText(), false);
            } else playerTwo = new GUIPlayer(blackName.getText(), false);
            BOARD.setPlayerOne(playerOne);
            BOARD.setPlayerTwo(playerTwo);
            if(playerOneUp)
                BOARD.currentPlayer = playerOne;
            else BOARD.currentPlayer = playerTwo;    
            MOVE_HISTORY.setText(MOVE_HISTORY.getText()
                    .replaceAll(oldPlayerOne.name, playerOne.name)
                    .replaceAll(oldPlayerTwo.name, playerTwo.name));
            MOVE_HISTORY.appendText("");
        });
        updatePlayers.setDisable(true);
        doubleDropDown.getChildren().addAll(whiteSelect, blackSelect);

        //Start and stop buttons
        HBox startStop = new HBox();
        startStop.setMinWidth(rightArea.getMaxWidth());
        Button playButton = new Button("Start");
        playButton.setAlignment(Pos.CENTER);
        Button stopButton = new Button("Stop");
        stopButton.setAlignment(Pos.CENTER);
        playButton.setMinWidth(rightArea.getMaxWidth() / 2);
        stopButton.setMinWidth(rightArea.getMaxWidth() / 2);
        startStop.getChildren().add(playButton);
        startStop.getChildren().add(stopButton);
        playButton.setDisable(true);
        playButton.setOnAction((event) -> {
            GAME_LOOP.start();
            BOARD.enable();
            playButton.setDisable(true);
            stopButton.setDisable(false);
            undoOneMove.setDisable(true);
            undoTwoMoves.setDisable(true);
            p1Label.setDisable(true);
            p2Label.setDisable(true);
            blackSelect.setDisable(true);
            whiteSelect.setDisable(true);
            updatePlayers.setDisable(true);
            whiteName.setDisable(true);
            blackName.setDisable(true);
        });
        stopButton.setOnAction((event) -> {
            Player tmp = BOARD.currentPlayer;
            GAME_LOOP.cancel();
            GAME_LOOP.reset();
            ComputerPlayer.reset();
            BOARD.currentPlayer = tmp;
            BOARD.disable();
            playButton.setDisable(false);
            stopButton.setDisable(true);
            undoOneMove.setDisable(false);
            undoTwoMoves.setDisable(false);
            p1Label.setDisable(false);
            p2Label.setDisable(false);
            blackSelect.setDisable(false);
            whiteSelect.setDisable(false);
            updatePlayers.setDisable(false);
            whiteName.setDisable(false);
            blackName.setDisable(false);
            BOARD.selected = null;
            BOARD.updateGraphics();
        });

        //add nodes to rightArea
        rightArea.getChildren().add(MOVE_HISTORY);
        rightArea.getChildren().add(startStop);
        rightArea.getChildren().add(undoOneMove);
        rightArea.getChildren().add(undoTwoMoves);
        rightArea.getChildren().add(playerSelectLabels);
        rightArea.getChildren().add(doubleDropDown);
        rightArea.getChildren().add(playerNameFields);
        rightArea.getChildren().add(updatePlayers);
        rightArea.getChildren().add(bigLogo);

        //New button
        Button newBtn = new Button("New");
        newBtn.setOnAction((event) -> {
            GAME_LOOP.cancel();
            GAME_LOOP.reset();
            try {
                ComputerPlayer.reset();
                BOARD.loadSaveState((SaveState) new ObjectInputStream(
                        new FileInputStream(new File("default.save"))).readObject());
                BOARD.updateGraphics();
                BOARD.setPlayerOne(playerOne);
                BOARD.setPlayerTwo(playerTwo);
                if(playerOne.isWhite)
                    BOARD.currentPlayer = playerOne;
                else BOARD.currentPlayer = playerTwo;    
                MOVE_HISTORY.clear();
                playButton.setDisable(true);
                stopButton.setDisable(false);
                undoOneMove.setDisable(true);
                undoTwoMoves.setDisable(true);
                p1Label.setDisable(true);
                p2Label.setDisable(true);
                blackSelect.setDisable(true);
                whiteSelect.setDisable(true);
                updatePlayers.setDisable(true);
                whiteName.setDisable(true);
                blackName.setDisable(true);
                BOARD.enable();
                MOVE_HISTORY.setText(playerOne + " vs " + playerTwo + "\n"
                        + new Date().toString() + "\n");
                GAME_LOOP.start();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(GameOfChess.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(GameOfChess.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        //Save button
        Button saveBtn = new Button("Save");
        saveBtn.setOnAction((event) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Game");
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                try {
                    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
                    SaveState saveState = BOARD.getSaveState();
                    saveState.setMoveHistoryText(MOVE_HISTORY.getText());
                    oos.writeObject(saveState);
                } catch (FileNotFoundException ex) {
                    JOptionPane.showMessageDialog(null, "File not found!");
                } catch (IOException ex) {
                    Logger.getLogger(GameOfChess.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        //Load button
        Button loadBtn = new Button("Load");
        loadBtn.setOnAction((event) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Load Game");
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                try {
                    GAME_LOOP.cancel();
                    GAME_LOOP.reset();
                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                    SaveState save = (SaveState) ois.readObject();
                    String historyText = save.getMoveHistoryText();
                    BOARD.loadSaveState(save);
                    BOARD.updateGraphics();
                    playerOne = BOARD.player1;
                    playerTwo = BOARD.player2;
                    MOVE_HISTORY.setText(historyText);
                    playButton.setDisable(true);
                    stopButton.setDisable(false);
                    undoOneMove.setDisable(true);
                    undoTwoMoves.setDisable(true);
                    BOARD.enable();
                    GAME_LOOP.start();
                } catch (FileNotFoundException ex) {
                    JOptionPane.showMessageDialog(null, "File not found!");
                    GAME_LOOP.start();
                } catch (IOException | ClassNotFoundException ex) {
                    Logger.getLogger(GameOfChess.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        //Spacing for right aligned elements in toolbar
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        //Difficulty combobox
        Label diffLabel = new Label("AI Difficulty:");
        ComboBox diffSelect = new ComboBox();
        diffSelect.getItems().addAll("Destitute", "Super Easy", "Easy", "Normal", "Hard");
        diffSelect.setValue("Normal");
        diffSelect.setOnAction((event) -> {
            String selected = diffSelect.getSelectionModel().getSelectedItem().toString();
            if (selected.equals("Destitute")) {
                ComputerPlayer.DEPTH.set(1);
                ComputerPlayer.USE_MOBILITY_SCORING.set(true);
            } else if (selected.equals("Super Easy")) {
                ComputerPlayer.DEPTH.set(4);
                ComputerPlayer.USE_MOBILITY_SCORING.set(false);
            } else if (selected.equals("Easy")) {
                ComputerPlayer.DEPTH.set(5);
                ComputerPlayer.USE_MOBILITY_SCORING.set(false);
            } else if (selected.equals("Normal")) {
                ComputerPlayer.DEPTH.set(5);
                ComputerPlayer.USE_MOBILITY_SCORING.set(true);
            } else if (selected.equals("Hard")) {
                ComputerPlayer.DEPTH.set(6);
                ComputerPlayer.USE_MOBILITY_SCORING.set(true);
            }
        });

        //Progress bar
        Label progLabel = new Label("Status:");
        ProgressBar progBar = new ProgressBar(0);
        progBar.setMinWidth(100);
        progBar.progressProperty().bind(GAME_LOOP.progressProperty());

        //Check indicator
        CHECK_ICON.setOpacity(0);

        //Show history checkbox
        Label showMovesLabel = new Label("Show History");
        CheckBox showMoves = new CheckBox();
        showMoves.setSelected(true);
        showMoves.setOnAction((event) -> {
            if (showMoves.isSelected()) {
                root.setRight(rightArea);
                primaryStage.sizeToScene();
            } else {
                root.setRight(null);
                primaryStage.sizeToScene();
            }
        });

        //Toolbar
        ToolBar tb = new ToolBar(newBtn, saveBtn, loadBtn, spacer,
                CHECK_ICON, diffLabel, diffSelect, progLabel, progBar,
                showMovesLabel, showMoves);
        root.setTop(tb);
        
        //vertical labels
        VBox leftArea = new VBox();
        for(int x=8;x>0;x--){
            Label tmp = new Label(String.valueOf(x));
            tmp.setMinHeight(80);
            tmp.setMinWidth(24);
            tmp.setFont(new Font("Arial", 22));
            
            tmp.setAlignment(Pos.CENTER);
            leftArea.getChildren().add(tmp);
        }
        root.setLeft(leftArea);
        
        //horizontal labels
        HBox bottomArea = new HBox();
        bottomArea.setMaxHeight(16);
        HBox horizontalLabelSpacer = new HBox();
        horizontalLabelSpacer.setMinWidth(24);
        bottomArea.getChildren().add(horizontalLabelSpacer);
        for(char c : ChessMove.COLUMNS) {
            Label tmp = new Label(String.valueOf(c));
            tmp.setMinWidth(80);
            tmp.setAlignment(Pos.CENTER);
            tmp.setFont(new Font("Arial", 22));
            bottomArea.getChildren().add(tmp);
        }
        root.setBottom(bottomArea);

        //Window
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setAlwaysOnTop(false);
        primaryStage.setResizable(false);
        primaryStage.setTitle(this.getClass().getPackage().getName() + " v1.0");
        primaryStage.getIcons().add(new Image("img/icon.png"));
        primaryStage.sizeToScene();

        //Close all threads on exit
        primaryStage.setOnCloseRequest((event) -> {
            ComputerPlayer.selfDestruct();
            GAME_LOOP.cancel();
        });

        //The "New" button will load this SaveState
        new ObjectOutputStream(new FileOutputStream("default.save"))
                .writeObject(BOARD.getSaveState());

        //All systems go
        primaryStage.show();
        GAME_LOOP.start();
    }
    */
}
