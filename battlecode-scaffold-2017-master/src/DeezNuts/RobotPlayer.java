package DeezNuts;
import battlecode.common.*;

public strictfp class RobotPlayer {
    static RobotController rc;
    static int numScoutMappingPhase = 100;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
    **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        // Here, we've separated the controls into a different method for each RobotType.
        // You can add the missing ones or rewrite this into your own control structure.
        switch (rc.getType()) {
            case ARCHON:
                runArchon();
                break;
            case GARDENER:
                runGardener();
                break;
            case SOLDIER:
                runSoldier();
                break;
            case LUMBERJACK:
                runLumberjack();
                break;
            case SCOUT:
            	runScout();
            	break;
        }
	}
    static void runScout()throws GameActionException{
        MapLocation initialMapLocation = rc.getLocation();

        //exploring
        MapLocation[] locationsVisited= new MapLocation[500];
        locationsVisited[0]=initialMapLocation;
        Direction stride= new Direction((float)(Math.random()*2*Math.PI));
        int indexMove=0;   
    	while (true){
    		try{  
    		    if (rc.getRoundNum() < 5000) {
    		    	MapLocation currentLocation=rc.getLocation();
    		        int x = (int) Math.floor(currentLocation.x);
    		        int y = (int) Math.floor(currentLocation.y);

  		        
		        	for (int index=1;index<=10;index++){
		        		
		        		int isOpen=rc.readBroadcast(index);
		        		
		        		//0 is open, 1 is Close
		        		if (isOpen==0){
		        			System.out.println("joining channel: "+index);
		        			//The channels from 11 to 20
		        			rc.broadcast(index,1);
		        			int currentIndex=10*index+1;
		        			
		        			
		        			//Initial Message to send
		        			String xInitial=Integer.toBinaryString(x);
		        			String yInitial=Integer.toBinaryString(y);

		        			rc.broadcast(currentIndex,Integer.parseInt(xInitial,2));
		        			currentIndex++;
		        			rc.broadcast(currentIndex,Integer.parseInt(yInitial,2));
		        			currentIndex++;
		        			int needToScan=169;
		        			//Always start with a 1 and ignore later for efficiency
		        			String toSend="1";
		        			for (int dx=x-6;dx<=x+6;dx++){
		        				for (int dy=y+6;dy>=y-6;dy--){
		        					needToScan--;
	        						if (rc.senseTreeAtLocation(new MapLocation(dx,dy))!=null){
	        							toSend=toSend+"1";	
	        						}
	        						else{
	        							toSend=toSend+"0";
	        						}
	        						
		        					if (toSend.length()==30){
		        						
		        						//System.out.println(toSend.length());
		        						rc.broadcast(currentIndex,Integer.parseInt(toSend,2));
		        						
		        						currentIndex++;
		        						toSend="1";
		        					}
		        					else{
		        						if (needToScan==0){
		        							//System.out.println(toSend.length());
			        						rc.broadcast(currentIndex,Integer.parseInt(toSend,2));
			        						
			        						currentIndex++;
			        						toSend="1";
		        						}
		        					}
	        					
		        					
		        				}
		        			}
		        			break;
		        		}	
    		        }
    		        ////////////////////////////////////////////////////////////////
    		        
    		    	if (currentLocation.distanceTo(locationsVisited[indexMove])<20){
    		    		//System.out.println("I am Close");
    		    		if (rc.canMove(stride,(float)2.5)){
    		    			rc.move(stride,(float)2.5);
    		    			
    		    			
    		    		}
    		    		else{
    		    			//System.out.println("can't move");
    		    			stride=new Direction((float)(Math.random()*2*Math.PI));
    		    			//System.out.println("struck");
    		    		}
    		    	}
    		    	else{
    		    		indexMove++;
    		    		stride=new Direction((float)(Math.random()*2*Math.PI));
    		    		locationsVisited[indexMove]=currentLocation;
    		    	}
    		    	
	        
    		    	Clock.yield();

    		        

    		        
    		       
    		    }
    		} catch (Exception e){
    			System.out.println("fuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuck");
    			
    			Clock.yield();
    		}
    	}
    }
    static void runArchon() throws GameActionException {
    	MapLocation globalInitialLocation=rc.getInitialArchonLocations(rc.getTeam())[0];
    	int x0=(int)globalInitialLocation.x-110;
    	int y0=(int)globalInitialLocation.y-110;
    	int lastChannelRead=0;
        Integer[][] globalMap=new Integer[220][220]; //x,y matrix, extra 10,10 is to avoid a sensing bug (scout senses out of scope areas)
        //Origin of globalMap is x0,y0

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	
                // Generate a random direction
                Direction dir = randomDirection();

                // Randomly attempt to build a gardener in this direction
                if (rc.canHireGardener(dir)) {
                     rc.hireGardener(dir);
                 }
                
                
                for (int elt=1;elt<11;elt++){
                	int isOpen=rc.readBroadcast(elt);
                	if (isOpen==1 && elt>lastChannelRead){
                		System.out.println("reading from channel: "+elt+","+lastChannelRead);
                		
                		int messageCounter=0;
                		int currentIndex=10*isOpen+1;
                		String initialXMessage=Integer.toBinaryString(rc.readBroadcast(currentIndex));
                		int xCenter=Integer.parseInt(initialXMessage,2);
                		currentIndex++;
                		String initialYMessage=Integer.toBinaryString(rc.readBroadcast(currentIndex));
                		int yCenter=Integer.parseInt(initialYMessage,2);
                		currentIndex++;
                		for (int stuff=currentIndex;stuff<=currentIndex+5;stuff++){
                			//Get rid of the first bit it was just there to prevent data loss.
                			String message=Integer.toBinaryString(rc.readBroadcast(stuff)).substring(1);	
                			for (int arrayIndex=0;arrayIndex<message.length();arrayIndex++){
                				messageCounter++;
                				int arrayElement=Character.getNumericValue(message.charAt(arrayIndex));
                				int row=messageCounter%13;
                				int col=messageCounter/13;
                				int xCoord=xCenter-6+col-x0;
                				int yCoord=yCenter+6-row-y0;
                				globalMap[xCoord][yCoord]=arrayElement;
                			}

                		}
                		lastChannelRead=elt;
                		break;
                	}
                }
                //Archon refreshes every open channel if no more channels are available.
                
            	if (rc.readBroadcast(10)==1 && lastChannelRead==10){
            		for (int elt=1;elt<11;elt++){
            			rc.broadcast(elt,0);
            		}
            		lastChannelRead=0;
            	}
                
                
                

                // Move randomly
                //tryMove(randomDirection());


                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    }

	static void runGardener() throws GameActionException {

        int maxNumScouts = 2;
        int currentNumScouts = 0;
        
        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

                // Listen for home archon's location
                int xPos = rc.readBroadcast(0);
                int yPos = rc.readBroadcast(1);
                MapLocation archonLoc = new MapLocation(xPos,yPos);

                // Generate a random direction
                Direction dir = randomDirection();

//                if (currentNumScouts < maxNumScouts && rc.canBuildRobot(RobotType.SCOUT, dir)) {
//                    rc.buildRobot(RobotType.SCOUT, dir);
//                    currentNumScouts++;
//                }
                
                // Randomly attempt to build a soldier or lumberjack in this direction
                if (rc.canBuildRobot(RobotType.SCOUT, dir) && Math.random()<0.5 && rc.canBuildRobot(RobotType.SCOUT,dir) ) {
                    rc.buildRobot(RobotType.SCOUT, dir);
                } 

                // Move randomly
                tryMove(randomDirection());

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
        }
    }

	static void runSoldier() throws GameActionException {
        System.out.println("I'm an soldier!");
        Team enemy = rc.getTeam().opponent();
        
        /*
         * State -> Function Mapping
         * 
         * 0 -> Attack 
         * 1 -> Retreat
         * 2 -> Herding
         * 3 -> Protection
         * 
         */
        
        boolean sawAnEnemy = false;
        MapLocation lastEnemyLocation = null;
        int state = 0;
        
        // The code you want your robot to perform every round should be in this loop
        while (true) {
            try {
                
                
                // redecide which state to enter, based on broadcast channel/environmental stimuli/local resources
                
                MapLocation myLocation = rc.getLocation();
                RobotInfo[] enemy_robots = rc.senseNearbyRobots(-1, enemy);
                RobotInfo[] team_robots = rc.senseNearbyRobots(-1, rc.getTeam());
                
                if (rc.getHealth() < 0.3*50) {
                    state = 1;
                }
                
                if (enemy_robots.length > 0) {
                    if (enemy_robots[0].type.equals(RobotType.GARDENER) || enemy_robots[0].type.equals(RobotType.ARCHON)) {
                        state = 0;
                    }
                }
                
                switch(state) {
                
                case 0: // attacking code
                    
                    // If there are some...
                    if (enemy_robots.length > 0) {
                        // And we have enough bullets, and haven't attacked yet this turn...
                        if (rc.canFireSingleShot()) {
                            // ...Then fire a bullet in the direction of the enemy.
                            Direction fireDirection = rc.getLocation().directionTo(enemy_robots[0].location);
                            
                            boolean callOffFire = false;
                            
                            for (RobotInfo team_robot : team_robots) {
                                Direction friendDirection = rc.getLocation().directionTo(team_robot.location);
                                
                                float angleBetween = friendDirection.degreesBetween(fireDirection);
                                
                                if (angleBetween < 40) {
                                    callOffFire = true;
                                    break;
                                }
                            }
                            
                            if (!callOffFire) {
                                rc.fireSingleShot(fireDirection);
                            }
                        }
                    }
                    
                    if (enemy_robots.length == 0) {
                        // move towards last enemy direction
                        if (sawAnEnemy) {
                            tryMove(myLocation.directionTo(lastEnemyLocation));
                            
                            if (Math.random() < 0.8) {
                                tryMove(myLocation.directionTo(lastEnemyLocation));
                            } else if (Math.random() < 0.5) {
                                tryMove(myLocation.directionTo(lastEnemyLocation).rotateLeftDegrees(90));
                            } else {
                                tryMove(myLocation.directionTo(lastEnemyLocation).rotateRightDegrees(90));
                            }
                        } else {
                            if (Math.random() < 0.8) {
                                tryMove(myLocation.directionTo(rc.getInitialArchonLocations(enemy)[0]));
                            } else if (Math.random() < 0.5) {
                                MapLocation[] toEnemyArchon = rc.getInitialArchonLocations(enemy);
                                Direction toEnemy = myLocation.directionTo(toEnemyArchon[0]).rotateLeftDegrees(90);
                                tryMove(toEnemy);
                            } else {
                                MapLocation[] toEnemyArchon = rc.getInitialArchonLocations(enemy);
                                Direction toEnemy = myLocation.directionTo(toEnemyArchon[0]).rotateRightDegrees(90);
                                tryMove(toEnemy);
                            }
                        }
                    } else {
                        // follow the enemy
                        tryMove(myLocation.directionTo(enemy_robots[0].location).rotateLeftDegrees(90));
                        sawAnEnemy = true;
                        lastEnemyLocation = enemy_robots[0].location;
                    }
                    
                    break;
                
                case 1: // retreating code
                    // If there are some...
                    if (enemy_robots.length > 0) {
                        // And we have enough bullets, and haven't attacked yet this turn...
                        if (rc.canFireSingleShot()) {
                            // ...Then fire a bullet in the direction of the enemy.
                            rc.fireSingleShot(rc.getLocation().directionTo(enemy_robots[0].location));
                        }
                    }
                    
                    if (enemy_robots.length == 0) {
                        // move randomly 
                        tryMove(randomDirection());
                    } else {
                        // run away from the enemy
                        tryMove(myLocation.directionTo(enemy_robots[0].location).rotateLeftDegrees((float) 135));
                    }
                    Clock.yield();
                    break;
                    
                case 2: // herding code
                    break;
                    
                case 3: // protection code
                    break;
                    
                default:
                    throw new RuntimeException("Shouldn't be here");
                }
                
                Clock.yield();
                
            } catch (Exception e) {
                System.out.println("Soldier Exception");
                e.printStackTrace();
            }
        }
    }

    static void runLumberjack() throws GameActionException {
        System.out.println("I'm a lumberjack!");
        Team enemy = rc.getTeam().opponent();

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

                // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
                RobotInfo[] robots = rc.senseNearbyRobots(RobotType.LUMBERJACK.bodyRadius+GameConstants.LUMBERJACK_STRIKE_RADIUS, enemy);

                if(robots.length > 0 && !rc.hasAttacked()) {
                    // Use strike() to hit all nearby robots!
                    rc.strike();
                } else {
                    // No close robots, so search for robots within sight radius
                    robots = rc.senseNearbyRobots(-1,enemy);

                    // If there is a robot, move towards it
                    if(robots.length > 0) {
                        MapLocation myLocation = rc.getLocation();
                        MapLocation enemyLocation = robots[0].getLocation();
                        Direction toEnemy = myLocation.directionTo(enemyLocation);

                        tryMove(toEnemy);
                    } else {
                        // Move Randomly
                        tryMove(randomDirection());
                    }
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Lumberjack Exception");
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns a random Direction
     * @return a random Direction
     */
    static Direction randomDirection() {
        return new Direction((float)Math.random() * 2 * (float)Math.PI);
    }

    /**
     * Attempts to move in a given direction, while avoiding small obstacles directly in the path.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        return tryMove(dir,20,3);
    }

    /**
     * Attempts to move in a given direction, while avoiding small obstacles direction in the path.
     *
     * @param dir The intended direction of movement
     * @param degreeOffset Spacing between checked directions (degrees)
     * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {

        // First, try intended direction
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }

        // Now try a bunch of similar angles
        boolean moved = false;
        int currentCheck = 1;

        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
                return true;
            }
            // Try the offset on the right side
            if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        return false;
    }

    /**
     * A slightly more complicated example function, this returns true if the given bullet is on a collision
     * course with the current robot. Doesn't take into account objects between the bullet and this robot.
     *
     * @param bullet The bullet in question
     * @return True if the line of the bullet's path intersects with this robot's current position.
     */
    static boolean willCollideWithMe(BulletInfo bullet) {
        MapLocation myLocation = rc.getLocation();

        // Get relevant bullet information
        Direction propagationDirection = bullet.dir;
        MapLocation bulletLocation = bullet.location;

        // Calculate bullet relations to this robot
        Direction directionToRobot = bulletLocation.directionTo(myLocation);
        float distToRobot = bulletLocation.distanceTo(myLocation);
        float theta = propagationDirection.radiansBetween(directionToRobot);

        // If theta > 90 degrees, then the bullet is traveling away from us and we can break early
        if (Math.abs(theta) > Math.PI/2) {
            return false;
        }

        // distToRobot is our hypotenuse, theta is our angle, and we want to know this length of the opposite leg.
        // This is the distance of a line that goes from myLocation and intersects perpendicularly with propagationDirection.
        // This corresponds to the smallest radius circle centered at our location that would intersect with the
        // line that is the path of the bullet.
        float perpendicularDist = (float)Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

        return (perpendicularDist <= rc.getType().bodyRadius);
    }
    
}
