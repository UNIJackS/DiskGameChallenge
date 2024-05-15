import ecs100.*;
import java.awt.Color;
import java.util.*;
import java.nio.file.*;
import java.io.*;



public class ChallengeDiskGame {
    // Constants for the game geometry: the disks in the shooting range should
    // all be in the rectangle starting at (0,0) with a width of 500 and a height of 150
    // The gun should be on the line at y = 300
    private static final double GAME_WIDTH = 500;
    private static final double SHOOTING_RANGE_Y = 150; // lowest point that a disk can be
    private static final double GUN_X = GAME_WIDTH/2;   // current x position of the gun
    private static final double GUN_Y = 300;
    private static final double SHOOTING_CIRCLE = GUN_Y-SHOOTING_RANGE_Y;

    //Constants for game logic
    private static final int DEFAULT_NUMBER_OF_SHOTS = 30;
    private static final int DEFAULT_NUMBER_OF_DISKS = 30;
    private int numShots = DEFAULT_NUMBER_OF_SHOTS;
    private int numDisks = DEFAULT_NUMBER_OF_DISKS;

    //Fields for the game state
    private double score = 0;                         // current score
    private int shotsRemaining = this.numShots;       // How many shots are left
    
    private int numDisksBuffer = this.numDisks; //buffer for disk number for score calculation

    private ArrayList <ChallengeDisk> disks = new ArrayList<ChallengeDisk>(); // The list of disks

    private ChallengeShot shotObject = new ChallengeShot(GAME_WIDTH,GUN_Y);

    //Requests for call back functions to raise
    private boolean saveRequested = false;
    private boolean loadRequested = false;
    private boolean restartRequested = false;
    private boolean shotRequested = false;
    private double shotRequestedX = 0;
    private double shotRequestedY = 0;

    private boolean roundRunning = true;




    //---------------------------------------- Call Back Functions ----------------------------------------
    //sets a buffer which is read at the start of a new round for the new number of disks
    public void setNumDisksCallback(double value){
        numDisksBuffer = (int)value;
    }
    //sets the number of shots to be used next restart
    public void setNumShotsCallBack(double value){
        numShots = (int)value;
    }
    //raises a restart request if the save game button is pressed to be used in the update function
    public void restartGameCallBack(){
        restartRequested = true;
    }
    //raises a load request if the load game button is pressed to be used in the update function
    public void loadGameCallBack(){
        loadRequested = true;
    }
    //raises a save request if the save game button is pressed to be used in the update function
    public void saveGameCallBack(){
        saveRequested =true;
    }
    //raises a shot request if the mouse is released inisde the firing zone
    public void mouseCallBack(String action, double x, double y){
        if(action.equals("released")){
            if(isWithinFiringZone(x, y)){
                shotRequested = true;
                shotRequestedX = x;
                shotRequestedY = y;
  
            }
         }

    }

    //---------------------------------------- update Functions ----------------------------------------
    //(functions are in order of when they are called in update)
    //This saves the game to a file
    //It is called from the update function assuming saveGameRequest is true
    //saveGameRequest is set by the saveGameCallBack function 
    public void saveGame(){
        BufferedWriter bw;
        boolean invalidInput = true;
        while(invalidInput){
           try{
               String fileName = null;
               while(fileName == null){
                   fileName = UIFileChooser.open();
               }

               FileWriter fw = new FileWriter(fileName, false);
               bw = new BufferedWriter(fw);
               invalidInput =false;


               bw.write(String.valueOf(score));
               bw.newLine();
               bw.write(String.valueOf(shotsRemaining));
               bw.newLine();
               bw.write(String.valueOf(numDisks));
               bw.newLine();

               for(int diskIndex =0; diskIndex < disks.size(); diskIndex +=1){
                   bw.write(String.valueOf(disks.get(diskIndex).toString()));
                   bw.newLine();
               }
               bw.close();

           }catch(IOException e){
               UI.println("invalid file name");
           }
        }
    }

    //This loads a save file
    //It is called from the update function assuming loadGameRequest is true
    //loadGameRequest is set by the loadGameCallBack function 
    public void loadGame(){
         //stops a shot if it is progress 
         shotObject.stop();
         boolean invalidInput = true;
         Scanner file = new Scanner("");
         while(invalidInput){
            try{
                file = new Scanner(Path.of(UIFileChooser.open()));
                invalidInput = false;
            }catch(IOException e){
                UI.println("error" + e);
            }
        }

        score = file.nextDouble();
        shotsRemaining = file.nextInt();
        numDisks = file.nextInt();
        disks = new ArrayList<ChallengeDisk>();
        while(file.hasNext()){
            disks.add(new ChallengeDisk(file.nextDouble(),file.nextDouble(),file.nextInt()));
        }
        file.close();
        return;
    }

    
    //This checks if the mouse click was inside the fireing zone
    //It is called from the update function
    public boolean isWithinFiringZone(double x, double y){
        // an easy approximation is to pretend it is the enclosing rectangle.
        // It is nicer to do a little bit of geometry and get it right
        return (x >= GUN_X-SHOOTING_CIRCLE/2) && (y >= GUN_Y-SHOOTING_CIRCLE/2)
        && (x <= GUN_X + SHOOTING_CIRCLE/2) && (y <= GUN_Y);
    }

    //This updates the users score 
    //It is called from the update function
    public void updateScoreCompletion(){
        
        // Hint: Remember to account for the broken disks
        /*# YOUR CODE HERE */
        double scoreTotal = 0;
       for(int diskIndex =0; diskIndex < disks.size(); diskIndex +=1){
           
           scoreTotal += disks.get(diskIndex).score();
       }
       //for all the destroyed disks
       scoreTotal += 150 * (numDisks - disks.size());

       score = scoreTotal;
       UI.printMessage("score:" + scoreTotal);
    }

    //This draws everything to the screen 
    //It is called from the update function
    public void draw(){
        UI.clearGraphics();
         //Redraw the boundary of the shooting range
         UI.setColor(Color.black);
         UI.drawRect(0,0, GAME_WIDTH, GUN_Y);
         UI.setColor(Color.gray);
         UI.drawLine(0, SHOOTING_RANGE_Y, GAME_WIDTH, SHOOTING_RANGE_Y);
 
         // Redraw the shooting zone in gray
         UI.setColor(Color.lightGray);
         UI.fillArc(GUN_X-SHOOTING_CIRCLE/2, GUN_Y-SHOOTING_CIRCLE/2, SHOOTING_CIRCLE, SHOOTING_CIRCLE, 0, 180);
 
         // Redraw the gun in black
         UI.setColor(Color.black);
         UI.fillRect(GUN_X-5, GUN_Y-5, 10, 10);

         for(int diskIndex =0; diskIndex < disks.size(); diskIndex +=1){
            disks.get(diskIndex).move(GAME_WIDTH,SHOOTING_RANGE_Y,5.0);
            disks.get(diskIndex).draw();
         }

         shotObject.draw();

         for(int currentRound =0; currentRound < shotsRemaining; currentRound +=1){
            UI.setColor(Color.red.darker());
            UI.fillRect(3,GUN_Y -4 - 4*currentRound, 3, 3);
         }

    }


    //---------------------------------------- Run Functions ----------------------------------------
    //(functions are in order of when they are called in run)
    //setsup the call back functions and screen
    //It is called from the run function
    public void setupGUI(){
        UI.setMouseListener(this::mouseCallBack);
        UI.addSlider("Number of Disks",2,60,DEFAULT_NUMBER_OF_DISKS,this::setNumDisksCallback);
        UI.addSlider("Number of Shots",2,60,DEFAULT_NUMBER_OF_SHOTS,this::setNumShotsCallBack);
        UI.addButton("Restart", this::restartGameCallBack);
        UI.addButton("Load Game", this::loadGameCallBack);
        UI.addButton("Save Game", this::saveGameCallBack);

        UI.addButton("Quit", UI::quit);
        UI.setDivider(0);
    }

    //This updates the disks list with new disks at the start of each round 
    //It is called from the reset function
    public void initialiseDisks(){
        disks = new ArrayList<ChallengeDisk>();
        for(int currentDisk =0; currentDisk < numDisks; currentDisk +=1){
            boolean invalidDisk = true;
            while(invalidDisk){
                
                int xPos = (int)(Math.random()*(GAME_WIDTH-20)+10);

                int yPos = (int)((Math.random()*(SHOOTING_RANGE_Y-20))+10);

                disks.add(new ChallengeDisk(xPos,yPos));

                invalidDisk = false;

                
                if( disks.size() > 1){
                    for(int diskIndex =0; diskIndex < disks.size()-1; diskIndex +=1){
                        if(disks.get(disks.size()-1).isOverlapping(disks.get(diskIndex))){
                            disks.get(disks.size()-1).damage();
                            disks.remove(disks.size()-1);
                            invalidDisk = true;
                        }
                    }
                }
                
            }
        }
    }

    //resets the game for a new round
    //It is called from the run function
    public void reset(){
        numDisks = numDisksBuffer;
        shotsRemaining = numShots;
        initialiseDisks();
        shotObject.stop();
    }
    
    //This is the core of the program and is called every frame
    //It handles the restart, save, load and fire requests, updates the score and redraws the screen
    //It is called from the run function
    public long update(long lastUpdateTime){
        if(disks.size() < 1 || shotsRemaining  < 1){
            roundRunning = false;
            return(0);
        }

        shotObject.update(disks);
        long currentTime = System.currentTimeMillis();
        if(currentTime-lastUpdateTime > 100){
            lastUpdateTime = System.currentTimeMillis();
            //handles the restart request from the call back fucntion
            if(restartRequested){  
                restartRequested =false;
                roundRunning = false;
            }
            //handles the save request from the call back fucntion
            if(saveRequested){ 
                saveRequested =false;
                saveGame();
            }
            //handles the load request from the call back fucntion
            if(loadRequested){ 
                loadRequested =false;   
                loadGame();
            }
            //handles the shot request from the call back fucntion
            if(shotRequested){
                shotRequested = false;  
                shotsRemaining = shotObject.start(shotRequestedX, shotRequestedY, shotsRemaining);
            }

            updateScoreCompletion();
            draw();
        }

        return(lastUpdateTime);
    }
    
    //This shows the user their score for 3 seconds
    //It is called from the run function
    public void showScore(){
        UI.setColor(Color.red);
        UI.setFontSize(24);
        UI.drawString("Your final score: " + score, GAME_WIDTH*1.0/3.0, SHOOTING_RANGE_Y*1.3);
        UI.sleep(3000);
    }

    //This is the main loop of the program and calls everything else
    public void run(){
        UI.println("numDisks"+numDisks);
        setupGUI();
        while(true){
            reset();
            roundRunning = true;
            long lastUpdateTime = System.currentTimeMillis();
            while(roundRunning){
                lastUpdateTime = update(lastUpdateTime);
            }
            showScore();
        }
    }

    public static void main(String[] args){
        ChallengeDiskGame dg = new ChallengeDiskGame();
        dg.run();
    }
}