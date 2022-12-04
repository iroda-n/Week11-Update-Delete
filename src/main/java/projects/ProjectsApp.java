package projects;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import projects.entity.Project;
import projects.exception.DbException;
import projects.service.ProjectService;

//import projects.dao.DbConnection;

public class ProjectsApp {
	
	private ProjectService projectService = new ProjectService();
	
	// @formatter:off
	private List<String> operations = List.of(
			"1) Add a project",
			"2) List Projects",
			"3) Select a project",
			"4) Update project details",
			"5) Delete a project");
	// @formatter:on
	
	private Scanner scanner = new Scanner (System.in);
	Project curProject;
	
	public static void main(String[] args) {
		ProjectsApp projectAppObj = new ProjectsApp();
		projectAppObj.processUserSelections();
	} //end main

	
	/**
	 * This method processes user selection based on user-input number
	 * Throw an exception if error is encountered
	 */
	private void processUserSelections() {
		boolean done = false;
		
		while (!done) {
			try {
				int selection = getUserSelection();
				switch (selection)
				{
				case -1:
					done = exitMenu();
					break;
				
				case 1:
					createProject();
					break;
				
				case 2:
					listProjects();
					break;
					
				case 3:
					selectProject();
					break;
					
				case 4:
					updateProjectDetails();
					break;
					
				case 5:
					deleteProject();
					break;
					
				default:
					System.out.println("\n" + selection + " is not a valid selection. Try again.\n");
				} //end switch statement
			}
			catch (Exception e) {
				System.out.println("Error: " + e.toString());
			} //end try-catch
		} //end while	
	} //end processUserSelections

	private void deleteProject() {
		listProjects();
		Integer projectId = getIntInput ("Enter ID of the project to delete: ");
		
		projectService.deleteProject(projectId);
		
		System.out.println("Project " + projectId + " was deleted successfully!");
		
		if (Objects.nonNull(curProject) && curProject.getProjectId().equals(projectId)) {
			curProject = null;
		}
		
		
	}


	private void updateProjectDetails() {
		if (Objects.isNull(curProject)) {
			System.out.println("\nPlease select a project.");
			return;
		}
		
		String projectName = getStringInput("Enter the project name [" + curProject.getProjectName() + "]");
		BigDecimal estimatedHours = getDecimalInput("Enter the estimated hours [" + curProject.getEstimatedHours() + "]");
		BigDecimal actualHours = getDecimalInput("Enter actual hours [" + curProject.getActualHours() + "]");
		Integer difficulty = getIntInput("Enter difficulty + [" + curProject.getDifficulty() + "]");
		String notes = getStringInput("Enter notes [" + curProject.getNotes() + "]");
		
		Project project = new Project();
		project.setProjectName(Objects.isNull(projectName) ? curProject.getProjectName() : projectName);
		project.setEstimatedHours(Objects.isNull(estimatedHours) ? curProject.getEstimatedHours() : estimatedHours);
		project.setActualHours(Objects.isNull(actualHours) ? curProject.getActualHours() : actualHours);
		project.setDifficulty(Objects.isNull(difficulty) ? curProject.getDifficulty() : difficulty);
		project.setNotes(Objects.isNull(notes) ? curProject.getNotes() : notes);
		
		project.setProjectId(curProject.getProjectId());
		
		projectService.modifyProjectDetails(project);
		
		curProject = projectService.fetchProjectById(curProject.getProjectId());
		
	}


	private void selectProject() {
		listProjects();
		Integer projectId = getIntInput ("Enter a project ID to select a project: ");
		
		//unselect the current project
		curProject = null;
		
		//retrieve project using user-entered ID
		curProject = projectService.fetchProjectById(projectId);
		
		if (Objects.isNull(curProject)) {
			System.out.println("Invalid project ID selected.");
		}
		
	}

	private void listProjects() {
		List<Project> projects = projectService.fetchAllProjects();
		System.out.println("\nProjects: ");
		projects.forEach(project -> System.out.println("  " + project.getProjectId() + ": " + project.getProjectName()));
		
	}

	private void createProject() {
		String projectName = getStringInput("Enter the project name ");
		BigDecimal estimatedHours = getDecimalInput("Enter estimated hours ");
		BigDecimal actualHours = getDecimalInput("Enter actual hours ");
		Integer difficulty = getIntInput("Enter the project difficulty (1-5) ");
		String notes = getStringInput("Enter the project notes ");
		Project project = new Project();
		
		project.setProjectName(projectName);
		project.setEstimatedHours(estimatedHours);
		project.setActualHours(actualHours);
		project.setDifficulty(difficulty);
		project.setNotes(notes);
		
		Project dbProject = projectService.addProject(project);
		
		System.out.println("You have successfully created project: " + dbProject);
	}

	private boolean exitMenu() {
		System.out.println("Exiting menu.");
		return true;
	}

	private int getUserSelection() {
		printOperations();
		
		Integer input = getIntInput("Enter a menu selection");
		
		if (input == null) {
			return -1;
		}
		else {
			return input;
		}
	}
	
	private BigDecimal getDecimalInput(String prompt) {
		String input  = getStringInput(prompt);
		if (Objects.isNull(input)) {
			return null;
		}
		
		try {
			return new BigDecimal(input).setScale(2);
		}
		catch (NumberFormatException nfe) {
			throw new DbException (input + " is not a valid decimal number.");
			
		}
	}


	private Integer getIntInput(String prompt) {
		String input  = getStringInput(prompt);
		if (Objects.isNull(input)) {
			return null;
		}
		
		try {
			return Integer.valueOf(input);
		}
		catch (NumberFormatException nfe) {
			throw new DbException (input + " is not a valid number. Try again. ");
			
		}
	}

	private String getStringInput(String prompt) {
		System.out.print(prompt + ": ");
		String input = scanner.nextLine();
		
		if (input.isBlank()) {
			return null;
		}
		else {
			return input.trim();
		}
	}

	private void printOperations() {
		System.out.println("These are the available selections. Press the Enter key to quit: ");
		operations.forEach(line -> System.out.println("   " + line));
		
		if (Objects.isNull(curProject)) {
			System.out.println("\nYou are not working with a project");
		}
		else {
			System.out.println("\nYou are working with project: " + curProject);
		}
		
	}
	
	

}
