package multiagent;

import java.util.*;
import java.io.*;

/**
 * This class implements a UAV.
 * 
 * ===================      README      ======================
 * 
 * NOT ALL THE ACTION IN THE CURRENT CLASS HAVE TO BE DEFINED.
 * Think carefully to what you choose to do and complete 
 * the action needed by your implementation.
 * IF YOU THINK that your implementation needs more methods
 * than those here proposed, you can implement them. 
 * Be careful to follow the guidelines and be ready to 
 * motivate your changes. 
 * ===========================================================
 * 
 * @version 1.1
 * @author Albani Dario
 * 
 * @version 1.0 - May 22, 2013
 * @author Federico Patota
 * @author Gabriele Buondonno
 */
public class Agent implements Serializable{
	private static final long serialVersionUID = 1L;

	/**Identifier for this agent.*/
	private final int id;

	/**current position of the agent.*/
	private Cell position;

	//cella vecchia
	private Cell old_position;

	/**task currently under execution by this agent.*/
	private Task currentTask;
	
	/**action currently under execution by this agent.*/
	private Action currentAction;

	/**Tasks assigned to any agent yet.*/
	private LinkedList<Task> pendingTasks = new LinkedList<Task>();

	/**instance of the current world class on which the agent is acting**/
	private World world;
	
	/**
	 * Constructor for this class.
	 * @param id identifier for this agent.
	 * @param c initial agent position. 
	 * @param world an instance of the world used to retrieve information and tasks
	 */
	public Agent(int id, Cell c, World world){
		this.id=id;
		this.world = world;
		position=c;
		old_position = null;
		currentAction = Action.publishNextTask;
	}

	/**
	 * Returns the agent identifier.
	 * @return this agent identifier.
	 */
	public int getId(){
		return id;
	}

	/**
	 * Returns the current position of the agent.
	 * @return the current position of this agent.
	 */
	public Cell getPosition(){
		return position;
	}

	/**
	 * Returns the task currently under execution by the agent.
	 * @return the task currently under execution by this agent.
	 */
	public Task getCurrentTask(){
		return currentTask;
	}

	/**
	 * Checks whether the pending tasks list is empty.
	 * @return true if the pending tasks list is empty, otherwise false.
	 */
	public boolean emptypTask(){
		return pendingTasks.isEmpty();
	}

	/**
	 * Adds this cell to the pending tasks list only if has weeds and is not harvested 
	 */
	public void addpTask(Task t){
		if (t.getCell().isWeed() && !t.getCell().isSprayed())
			pendingTasks.add(t);
	}

	/**
	 * Removes this location from the pending tasks list.
	 */
	public void removepTask(Task t){
		pendingTasks.remove(t);
	}

	/**
	 * Returns a pending task from the pending tasks list, according to some priority.
	 * If the list is empty, it returns null.
	 * @return the next pending task to be auctioned.
	 */
	public Task getNextpTask(){
		if(pendingTasks.isEmpty())return null;
		return pendingTasks.get(0);
	}

	/**
	 * The world calls this method to ask the agent for the next action to be executed.
	 * The agent returns the action currently under execution.
	 * @return the action that needs to be executed.
	 */
	public Action nextAction(){
		return currentAction;
	}

	/**
	 *
	 * Deletes current task setting it to null.
	 */
	public void cancelTask(){
		this.currentTask = null;
	}

	//quando sono diretto verso il muro, e non posso scegliere tra le tre direzioni ammissibili solitamente
	public Cell escapeMove(int myRow,int myCol) {
		List<Cell> accessible = new ArrayList<Cell>();
		//diag su sx
		if(this.world.isValid(myRow + 1, myCol+1)) {
			accessible.add(this.world.getCell(myRow + 1, myCol+1));
		}//su
		if(this.world.isValid(myRow + 1, myCol)){
			accessible.add(this.world.getCell(myRow + 1, myCol));
		}//sx
		if(this.world.isValid(myRow, myCol+1)){
			accessible.add(this.world.getCell(myRow, myCol+1));
		}//diag giu dx
		if(this.world.isValid(myRow -1, myCol-1)) {
			accessible.add(this.world.getCell(myRow - 1, myCol-1));
		}//giu
		if(this.world.isValid(myRow - 1, myCol)){
			accessible.add(this.world.getCell(myRow - 1, myCol));
		}//dx
		if(this.world.isValid(myRow, myCol-1)){
			accessible.add(this.world.getCell(myRow, myCol-1));
		}//diag giu sx
		if(this.world.isValid(myRow - 1, myCol+1)){
			accessible.add(this.world.getCell(myRow - 1, myCol+1));
		}//diag su dx
		if(this.world.isValid(myRow+1, myCol-1)){
			accessible.add(this.world.getCell(myRow+1, myCol-1));
		}
		//pesco random tra gli elementi dell'array
		int selectable = accessible.size() - 1;
		int nextMove = 0 + (int)(Math.random() * ((selectable - 0) + 1));
		return accessible.get(nextMove);
	}

	/** 
	 * 
	 * The simulator asks the agent to allocate the pending tasks.
	 * The pending tasks are either the list of pending tasks available in the current class
	 * or the task still available in the world (this depend to your implementation).
	 * The tasks are immediately assigned according to the specification of your exercise.
	 * The agent list is provided by the simulator.
     *
	 * @param agents the agents list.
	 */

	//tra le tre direzioni ammissibili, in base al potenziale associo una probabilità a ciascuna e scelgo random
	public Cell chooseByProbability(List<Cell> actual) {
		List<ConfCell> toSort = new ArrayList<ConfCell>();
		for(int i=0;i<actual.size();i++){
			ConfCell p = new ConfCell(actual.get(i),actual.get(i).getAttractivePot() - actual.get(i).lastVisited());
			toSort.add(p);
		}
		//le ordino in base al valore del potenziale
		Collections.sort(toSort, new Comparator<ConfCell>() {
  			public int compare(ConfCell c1, ConfCell c2) {
    			if (c1.value > c2.value) return -1;
    			if (c1.value < c2.value) return 1;
    			return 0;
  		}	});

		//inserisco copie delle stesse, in baso al loro valore(per aumentare la prob di essere scelte)
		List<ConfCell> sorted = new ArrayList<ConfCell>();
		if(this.world.isRepulseEnable()){
			for(int i=0;i<toSort.size();i++){
				if(i == 0) {
					sorted.add(toSort.get(i));
					sorted.add(toSort.get(i));
					sorted.add(toSort.get(i));
					sorted.add(toSort.get(i));
					sorted.add(toSort.get(i));
					sorted.add(toSort.get(i));
				}
				else if(i == 1) {
					sorted.add(toSort.get(i));
					sorted.add(toSort.get(i));
				}
				else if(i == 2)
					sorted.add(toSort.get(i));
				else
					sorted.add(toSort.get(i));
			}
		}
		else {
			sorted = toSort;
		}

		int selectable = sorted.size() - 1;
		int nextMove = 0 + (int)(Math.random() * ((selectable - 0) + 1));
		return sorted.get(nextMove).myself;
	}	

	//scelgo la nuova direzione in cui muovermi. 3 opzioni disponibili, per evitare di rompere la formazione
	public Cell calculateNewDir(int dir_x,int dir_y){
		int myRow = this.position.getRow();
		int myCol = this.position.getCol();
		List<Cell> accessible = new ArrayList<Cell>();
		Cell uno = null;
		Cell due = null;
		Cell tre = null;
		//
		if(dir_x > 0 && dir_y > 0) {
			//diagonale su sx
			if(this.world.isValid(myRow + 1, myCol+1)) {
				uno = this.world.getCell(myRow + 1, myCol+1);
			}
			if(this.world.isValid(myRow + 1, myCol)){
				due = this.world.getCell(myRow + 1, myCol);
			}
			if(this.world.isValid(myRow, myCol+1)){
				tre = this.world.getCell(myRow, myCol+1);
			}
		}	
		else if(dir_x == 0 && dir_y > 0) {
			//sx
			if(this.world.isValid(myRow, myCol+1))
				uno = this.world.getCell(myRow, myCol+1);
			if(this.world.isValid(myRow + 1, myCol+1))
				due = this.world.getCell(myRow + 1, myCol+1);
			if(this.world.isValid(myRow - 1, myCol+1))
				tre = this.world.getCell(myRow - 1, myCol+1);
		}
		else if(dir_x > 0 && dir_y == 0) {
			//su
			if(this.world.isValid(myRow + 1, myCol))
				uno = this.world.getCell(myRow + 1, myCol);
			if(this.world.isValid(myRow + 1, myCol+1))
				due = this.world.getCell(myRow + 1, myCol+1);
			if(this.world.isValid(myRow + 1, myCol-1))
				tre = this.world.getCell(myRow + 1, myCol-1);
		}
		else if(dir_x < 0 && dir_y < 0) {
			//diag giu dx//da 
			if(this.world.isValid(myRow -1, myCol-1))
				uno = this.world.getCell(myRow - 1, myCol - 1);
			if(this.world.isValid(myRow-1, myCol))
				due = this.world.getCell(myRow - 1, myCol);
			if(this.world.isValid(myRow, myCol-1))
				tre = this.world.getCell(myRow, myCol - 1);
		}
		else if(dir_x == 0 && dir_y < 0) {
			//dx
			if(this.world.isValid(myRow, myCol-1))
				uno = this.world.getCell(myRow, myCol-1);
			if(this.world.isValid(myRow+1, myCol-1))
				due = this.world.getCell(myRow + 1, myCol - 1);
			if(this.world.isValid(myRow-1, myCol-1))
				tre = this.world.getCell(myRow - 1, myCol -1);
		}
		else if(dir_x < 0 && dir_y == 0) {
			//giu
			if(this.world.isValid(myRow-1, myCol))
				uno = this.world.getCell(myRow - 1, myCol);
			if(this.world.isValid(myRow-1, myCol-1))
				due = this.world.getCell(myRow - 1, myCol - 1);
			if(this.world.isValid(myRow-1, myCol+1))
				tre = this.world.getCell(myRow - 1, myCol +1);
		}
		else if(dir_x > 0 && dir_y < 0) {
			//diag su dx
			if(this.world.isValid(myRow+1, myCol-1))
				uno = this.world.getCell(myRow + 1, myCol - 1);
			if(this.world.isValid(myRow+1, myCol))	
				due = this.world.getCell(myRow + 1, myCol);
			if(this.world.isValid(myRow, myCol-1))
				tre = this.world.getCell(myRow, myCol-1);
		}
		else if(dir_x == 0 && dir_y == 0) {
			//all'inizio,vai su
			if(this.world.isValid(myRow, myCol+1))
				uno = this.world.getCell(myRow, myCol+1);
			if(this.world.isValid(myRow+1, myCol+1))
				due = this.world.getCell(myRow + 1, myCol+1);
			if(this.world.isValid(myRow-1, myCol+1))
				tre = this.world.getCell(myRow - 1, myCol+1);
		}
		else {
			//diag giu sx
			if(this.world.isValid(myRow, myCol+1)) {
				uno = this.world.getCell(myRow, myCol+1);
			}
			if(this.world.isValid(myRow - 1, myCol)) {
				due = this.world.getCell(myRow - 1, myCol);
			}
			if(this.world.isValid(myRow - 1, myCol+1)) {
				tre = this.world.getCell(myRow - 1, myCol+1);
			}
		}
		//controllo che non siano null, ne ci siano altri agenti sopra
		Cell whereToMove = null;
		if(uno != null){
			if((uno.getAgent() == -1)) {			
				accessible.add(uno);
			}
		}
		if(due != null) {
			if(due.getAgent() == -1) {
				accessible.add(due);
			}
		}
		if(tre != null) {
		    if(tre.getAgent() == -1) {
				accessible.add(tre);
			}
		}
		//se l'array è pieno, scelgo random, altrimento escapeMove perchè sono vicino ad un muro
		if(!accessible.isEmpty()) {
			whereToMove = chooseByProbability(accessible);
		}
		else {
			whereToMove = escapeMove(myRow,myCol);
		}
		return whereToMove;
	}


	
	public void assignTasks(List<Agent> agents){
		//se sono su un erba, la elimino
		if(this.position.isWeed()) {
			this.currentAction = Action.spray;
			this.currentTask = this.position.getTask();
			return;
		}
		//devo scegliere un leader e seguirlo
		List<Agent> others = world.getAllAgents();
		List<Agent> flock_agent = new ArrayList<Agent>();
		//calcolo la direzione in cui sto andando
		int direction_x = 0;
		int direction_y = 0;
		int near_x = 0;
		int near_y = 0;
		if(old_position != null) {
			direction_x = this.position.getRow() - this.old_position.getRow();
			direction_y = this.position.getCol() - this.old_position.getCol();
		}
		else {
			direction_x = 1;
			direction_y = 0;
		}

		//flock
		Agent near = null;
		double near_dis = 1000;
		for(int i = 0; i < others.size();i++) {
			if(others.get(i).getId() == this.id)
				continue;
			double distance = Math.sqrt(Math.pow(others.get(i).position.getRow() - this.position.getRow(),2) + Math.pow(others.get(i).position.getCol() - this.position.getCol(),2));
			//se sono nella vicinanze li setto come compagni di flock
			if(distance <= 4) {
				flock_agent.add(others.get(i));
				int distance_x = others.get(i).position.getRow() - this.position.getRow();
				int distance_y = others.get(i).position.getCol() - this.position.getCol();
				double cos_angle = distance_x*direction_x + distance_y*direction_y;
				cos_angle = cos_angle/(Math.sqrt(Math.pow(distance_x,2) + Math.pow(distance_y,2))*Math.sqrt(Math.pow(direction_x,2) + Math.pow(direction_y,2)));
				
				if(Math.toDegrees(Math.acos(cos_angle)) <= 75 && Math.toDegrees(Math.acos(cos_angle)) >= 0) {
					if(distance < near_dis) {
						near = others.get(i);
						near_dis = distance;
						near_x = distance_x;
						near_y = distance_y;
					}
				}
			}
			else {
				continue;
			}
		}
		//se sono in formazione e dietro a qualcuno, lo seguo. (Posso sempre muovermi verso di lui, scegliendo fra 3 direzioni)
		if(near != null && this.world.isFlockEnable()) {
			Cell toMove = calculateNewDir(near_x,near_y);
			this.currentTask = toMove.getTask();
			this.currentAction = Action.moveToLocation;
			old_position = position;
		}
		//altrimenti mi muovo sempre verso 3 direzioni(in base alla mia attuale), scegliendo randomicamente 
		//Solo 3 per evitare di rompere il flock
		else {
			Cell toMove = calculateNewDir(direction_x,direction_y);
			this.currentTask = toMove.getTask();
			this.currentAction = Action.moveToLocation;
			old_position = position;
		}
	}
	
	/** 
	 * CNP - implement your own logic (e.g. for CNP the agent is the manager of task)
	 * 
	 * The simulator asks the agent to allocate the pending tasks.
	 * The pending tasks are either the list of pending tasks available in the current class
	 * or the task still available in the world (this depend to your implementation).
	 * The tasks are immediately assigned according to the specification of your exercise.
	 * The agent list is provided by the simulator.
	 *
	 * @param agents the agents list.
	 */
	public void assignTasks(List<Agent>agents, Task task){
		System.out.print("TODO: you should find a way to assign a new task\n");
		for(Agent a : agents){
			a.bidForTask(task);
			//continue
		}	
	}

	/**
	 * 
	 * This method is called by the simulation environment to inform
	 * the agent about the outcome of its action, as provided by the World.
	 * @param nextPosition the agent new position.
	 * @param nextTask a new task discovered by this agent. If null, there is no next task.
	 */
	public void updateState(Cell nextPosition, Task nextTask){
		//settaggio potenziale repulsivo e attrattivo
		this.position = nextPosition;
		this.position.setLastVisited();
		this.currentTask = nextTask;
		if(this.position.isWeed() && this.world.isAttractEnable()) {
			int this_row = this.position.getRow();
			int this_col = this.position.getCol();
			for(int row=-5;row<5;row++){
				for(int col=-5;col<5;col++){
					if(this.world.isValid(this_row + row,this_col + col)) {
						if(col == 0 && row == 0)
							this.world.getCell(this_row + row,this_col + col).setAttractivePot(3.0f);
						//il potenziale attrattivo, decresce in base alla distanza dall'epicentro
						else {
							double val = 3.0f - 0.5f*Math.sqrt((row)*(row) + (col)*(col));
							this.world.getCell(this_row + row,this_col + col).setAttractivePot(val);
						}
					}

				}
			}
			
		}
		
		//Some tips:
		// If the agent is in the same cell as nextPosition maybe it needs to 
		// either move or check/spray
		// Do not forget the noOp operation that is used to terminate the simulation (is one of the condition in or)
		//System.out.print("TODO: maybe something like this.currentAction = ??? \n");
		//System.out.print("TODO: I should do something\n");
		
	}


	/** 
	 * 
	 * The requests for Bids are issued by the agent.
	 * In the first implementation an agent can only bid if it has no current task.
	 * @param task the task which the request is issued for.
	 * @return a bid for the given task.
	 */
	public Bid bidForTask(Task task){
		System.out.print("TODO: are you dealing with auctions and CNP?\n");
		// TODO Complete
		return null;
	}

	/**
	 *  
	 * The acceptance requests are issued by the agent.
	 * In the first implementation an agent can only accept if it has no current task.
	 * @param task the task which the request is issued for.
	 * @return true if the task is accepted.
	 */
	public boolean acceptTask(Task task){
		System.out.print("TODO: how am I supposed to accept the task?\n");
		// TODO Complete
		return false;
	}


	public void setInitialTasks(LinkedList<Task> initial) {
		this.pendingTasks  = initial;
	}
}
