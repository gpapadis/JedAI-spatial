package dit.anonymous.webapp.models;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="workflow_results")
public class WorkflowResults {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(updatable = false, nullable = false, unique = true, name="id")	
	private int id;
		
	@Column (name = "workflowid")
	private int workflowID;
	
	@Column (name = "input_instances")
	private int inputInstances;
	
	@Column (name = "clusters")
	private int clusters;
	
	@Column (name = "time")
	private double[] time;
	
	@ElementCollection
	@Column (name = "method_names")
	private List<String> methodNames;
	
	@Column (name = "recall")
	private double[] recall;
	
	@Column (name = "precision")
	private double[] precision;
	
	@Column (name = "fmeasure")
	private double[] fmeasure;

	@Column (name = "auc")
	private double auc;
	
	@Column (name = "existing_duplicates")
	private double existingDuplicates;
	
	@Column (name = "detected_duplicates")
	private double detectedDuplicates;
	
	@Column (name = "total_matches")
	private double totalMatches;
	
	
	public WorkflowResults() {
		
	}
	
	public WorkflowResults(int workflowID, int inputInstances, int clustes, double[] times, List<String> methodNames,
			double[] recall, double[] precision, double[] fmeasure, double auc, double existingDuplicates, double detectedDuplicates, double totalMatches){

		this.setWorkflowID(workflowID);
		this.setInputInstances(inputInstances);
		this.setClusters(clustes);
		this.setTime(times);
		this.setMethodNames(methodNames);
		this.setRecall(recall);
		this.setPrecision(precision);
		this.setFmeasure(fmeasure);
		this.setExistingDuplicates(existingDuplicates);
		this.setDetectedDuplicates(detectedDuplicates);
		this.setTotalMatches(totalMatches);
		this.setAuc(auc);
	}
	
	public void update(int workflowID, int inputInstances, int clustes, double[] times, List<String> methodNames,
			double[] recall, double[] precision, double[] fmeasure, double auc, double existingDuplicates, double detectedDuplicates, double totalMatches) {

		this.setWorkflowID(workflowID);
		this.setInputInstances(inputInstances);
		this.setClusters(clustes);
		this.setTime(times);
		this.setMethodNames(methodNames);
		this.setRecall(recall);
		this.setPrecision(precision);
		this.setFmeasure(fmeasure);
		this.setExistingDuplicates(existingDuplicates);
		this.setDetectedDuplicates(detectedDuplicates);
		this.setTotalMatches(totalMatches);
		this.setAuc(auc);
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public int getWorkflowID() {
		return workflowID;
	}
	public void setWorkflowID(int workflowID) {
		this.workflowID = workflowID;
	}
	public int getInputInstances() {
		return inputInstances;
	}
	public void setInputInstances(int inputInstances) {
		this.inputInstances = inputInstances;
	}
	public int getClusters() {
		return clusters;
	}
	public void setClusters(int clusters) {
		this.clusters = clusters;
	}
	public double[] getTime() {
		return time;
	}
	public void setTime(double[] time) {
		this.time = time;
	}
	public List<String> getMethodNames() {
		return methodNames;
	}
	public void setMethodNames(List<String> methodNames) {
		this.methodNames = methodNames;
	}
	public double[] getRecall() {
		return recall;
	}
	public void setRecall(double[] recall) {
		this.recall = recall;
	}
	public double[] getPrecision() {
		return precision;
	}
	public void setPrecision(double[] precision) {
		this.precision = precision;
	}
	public double[] getFmeasure() {
		return fmeasure;
	}
	public void setFmeasure(double[] fmeasure) {
		this.fmeasure = fmeasure;
	}
	public void setAuc(double auc) {
		this.auc = auc;
	}
	public double getAuc() {
		return auc;
	}

	public double getExistingDuplicates() {
		return existingDuplicates;
	}

	public void setExistingDuplicates(double existingDuplicates) {
		this.existingDuplicates = existingDuplicates;
	}

	public double getDetectedDuplicates() {
		return detectedDuplicates;
	}

	public void setDetectedDuplicates(double detectedDuplicates) {
		this.detectedDuplicates = detectedDuplicates;
	}

	public double getTotalMatches() {
		return totalMatches;
	}

	public void setTotalMatches(double totalMatches) {
		this.totalMatches = totalMatches;
	}
	
}
