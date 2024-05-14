import java.util.ArrayList;

import ecs100.UI;

public class Bullet {
    private boolean noShotInProgress;
    private long timeSinceLastUpdate;

    private double xPosInital;
    private double yPosInital;

    private double GAME_WIDTH;

    private double xpos;
    private double ypos;

    private double step_X;


    // GAME_WIDTH is the width of the playable are
    // GUN_Y is the height of the playable are from the top of the screen
    public Bullet(double GAME_WIDTH, double GUN_Y){
        this.GAME_WIDTH = GAME_WIDTH;
        xPosInital = GAME_WIDTH/2;
        yPosInital = GUN_Y;
        xpos = GAME_WIDTH/2;
        ypos = GUN_Y;

        noShotInProgress = true;
    }

    public void stopShot(){
        noShotInProgress = true;
        xpos = xPosInital;
        ypos = yPosInital;
    }
    

    //attempts to initaite a shot if one is not already in progress it succedes
    //if the shot is sucessfully initated it decreasese the shots remaining by one
    public int startShot(double x,double y,int shotsRemaining){
        if(noShotInProgress){
            shotsRemaining -= 1;
            xpos = xPosInital;
            ypos = yPosInital;
            step_X = (xPosInital-x)/(y-yPosInital);
            timeSinceLastUpdate = System.currentTimeMillis();
            noShotInProgress = false;
        }

        return shotsRemaining;
    }

    //called to update the shot every frame
    public ArrayList <Disk> update(ArrayList <Disk> disks){
        //if there is a shot in progress then update it 
        if(!noShotInProgress){
            //if the bullet is off the screen the shot is no longer in progress
            if((xpos < 0 || ypos < 0 || xpos > GAME_WIDTH)){
                this.stopShot();
            }
            //if the time since the last update is greater than 2 milliseconds update its position
            if(System.currentTimeMillis() -timeSinceLastUpdate > 2){
                timeSinceLastUpdate = System.currentTimeMillis();
                ypos -= 1;
                xpos += step_X;
            }

            if(getHitDisk(xpos,ypos,disks) != null){
                Disk diskThatGotShot = getHitDisk(xpos,ypos,disks);
                diskThatGotShot.damage();
                if(diskThatGotShot.isBroken()){
                    damageNeighbours(diskThatGotShot,disks);
                }
                this.stopShot();
             }
        }

        return(disks);
    }

    //draws the bullet to the screen
    public void draw(){
        UI.drawLine(xPosInital, yPosInital, xpos, ypos);
    }

    //checks if the shot has hit a disk
    private Disk getHitDisk(double shotX, double shotY,ArrayList <Disk> disks){         
        for(int diskIndex =0; diskIndex < disks.size(); diskIndex +=1){
            if(disks.get(diskIndex).isOn(shotX,shotY)){
               return disks.get(diskIndex);
            }
        }
        return null;
    }

    //damadges the nabours if a disk if a disk expolodes
    private void damageNeighbours(Disk disk,ArrayList <Disk> disks){
       disk.explode();
       disks.remove(disk);
       for(int diskIndex =0; diskIndex < disks.size()-1; diskIndex +=1){
           if(disks.get(diskIndex).isWithinRange(disk)){
               disks.get(diskIndex).damage();
               if(disks.get(diskIndex).isBroken()){
                   damageNeighbours(disks.get(diskIndex),disks);
               }
           }
       }
    }

}
