package multiagent;

import java.io.*;

//classe creata per fare un sort delle celle in base al valore del potenziale(attrattivo-repulsivo)
public class ConfCell{
	public Cell myself;
	public double value;


	public ConfCell(Cell actual, double val){
		this.myself = actual;
		this.value = val;
	}
}