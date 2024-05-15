import java.util.ArrayList;

import ecs100.UI;

public class ChallengeShot {
    private boolean noShotInProgress;
    private long timeSinceLastUpdate;

    private double initalXPos;
    private double initalYPos;

    private double xpos;
    private double ypos;

    private double GAME_WIDTH;
    private double step_X;


    // GAME_WIDTH is the width of the playable area
    // GUN_Y is the height of the playable are from the top of the screen
    public ChallengeShot(double GAME_WIDTH, double GUN_Y){
        this.GAME_WIDTH = GAME_WIDTH;
        initalXPos = GAME_WIDTH/2;
        initalYPos = GUN_Y;
        xpos = GAME_WIDTH/2;
        ypos = GUN_Y;

        noShotInProgress = true;
    }

    //draws the bullet to the screen.
    //It is called from the diskgame file in the draw function.
    public void draw(){
        UI.drawLine(initalXPos, initalYPos, xpos, ypos);
    }

    //This is used to stop a shot in progress.
    //It is called from diskgame file in the load and reset functions.
    //It is also called in this file from the update function.
    public void stop(){
        noShotInProgress = true;
        xpos = initalXPos;
        ypos = initalYPos;
    }
    

    //attempts to start a shot if one is not already in progress.
    //If the shot is sucessfully initated it decreasese the shots remaining by one.
    public int start(double x,double y,int shotsRemaining){
        if(noShotInProgress){
            shotsRemaining -= 1;
            xpos = initalXPos;
            ypos = initalYPos;
            step_X = (initalXPos-x)/(y-initalYPos);
            timeSinceLastUpdate = System.currentTimeMillis();
            noShotInProgress = false;
        }

        return shotsRemaining;
    }

    //called to update the shot every frame
    public ArrayList <ChallengeDisk> update(ArrayList <ChallengeDisk> disks){
        //if there is a shot in progress then update it 
        if(!noShotInProgress){
            //if the bullet is off the screen the shot is no longer in progress
            if((xpos < 0 || ypos < 0 || xpos > GAME_WIDTH)){
                this.stop();
            }
            //if the time since the last update is greater than 2 milliseconds update its position
            if(System.currentTimeMillis() -timeSinceLastUpdate > 2){
                timeSinceLastUpdate = System.currentTimeMillis();
                ypos -= 1;
                xpos += step_X;
            }

            if(getHitDisk(xpos,ypos,disks) != null){
                ChallengeDisk diskThatGotShot = getHitDisk(xpos,ypos,disks);
                diskThatGotShot.damage();
                if(diskThatGotShot.isBroken()){
                    damageNeighbours(diskThatGotShot,disks);
                }
                this.stop();
             }
        }

        return(disks);
    }



    //checks if the shot has hit a disk
    private ChallengeDisk getHitDisk(double shotX, double shotY,ArrayList <ChallengeDisk> disks){         
        for(int diskIndex =0; diskIndex < disks.size(); diskIndex +=1){
            if(disks.get(diskIndex).isOn(shotX,shotY)){
               return disks.get(diskIndex);
            }
        }
        return null;
    }

    //damadges the Neighbours of a disk if a disk if a disk expolodes
    //Takes the disk that exploded and the current list of disks
    private void damageNeighbours(ChallengeDisk disk,ArrayList <ChallengeDisk> disks){
        //Plays the expolsion animation
       disk.explode();
       //removes it from the list (this must happen before the following loop or it will loop infintly)
       disks.remove(disk);
       //loops through all the disks left in the disk and checks if they are in the expolsion range
       for(int diskIndex =0; diskIndex < disks.size()-1; diskIndex +=1){
           if(disks.get(diskIndex).isWithinRange(disk)){
                //It then damadges these disks and checks if they are broken
               disks.get(diskIndex).damage();
               if(disks.get(diskIndex).isBroken()){
                    //recursivly calls the function if a disk that got damadged by an expolstion also expolodes
                   damageNeighbours(disks.get(diskIndex),disks);
               }
           }
       }
    }

}
