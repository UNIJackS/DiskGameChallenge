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


    public Bullet(double GAME_WIDTH, double GUN_Y){
        this.GAME_WIDTH = GAME_WIDTH;
        xPosInital = GAME_WIDTH/2;
        yPosInital = GUN_Y;
        xpos = GAME_WIDTH/2;
        ypos = GUN_Y;

        noShotInProgress = true;
    }

    public boolean isShotInProgress(){
        return noShotInProgress;
    }

    public void stopShot(){
        noShotInProgress = true;
        xpos = xPosInital;
        ypos = yPosInital;
    }
    

    //called to initaite a shot 
    public void startShot(double x,double y){
        if(noShotInProgress){
            xpos = xPosInital;
            ypos = yPosInital;
            step_X = (xPosInital-x)/(y-yPosInital);
            timeSinceLastUpdate = System.currentTimeMillis();
            noShotInProgress = false;
        }
    }

    //called to update the shot every frame
    public ArrayList <Disk> update(ArrayList <Disk> disks){
        //if there is a shot in progress then update it 
        if(!noShotInProgress){
            //if the bullet is off the screen the shot is no longer in progress
            if((xpos < 0 || ypos < 0 || xpos > GAME_WIDTH)){
                this.stopShot();
            }
            //if the time since the last update is greater than 5 milliseconds update its position
            if(System.currentTimeMillis() -timeSinceLastUpdate > 5){
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
    public Disk getHitDisk(double shotX, double shotY,ArrayList <Disk> disks){         
        for(int diskIndex =0; diskIndex < disks.size(); diskIndex +=1){
            if(disks.get(diskIndex).isOn(shotX,shotY)){
               return disks.get(diskIndex);
            }
        }
        return null;
    }

    //damadges the nabours if a disk if a disk expolodes
    public void damageNeighbours(Disk disk,ArrayList <Disk> disks){
       disk.explode(true);
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
