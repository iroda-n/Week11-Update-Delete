package projects.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;
import provided.util.DaoBase;

public class ProjectDao extends DaoBase {
	
	private static final String CATEGORY_TABLE = "category";
	private static final String MATERIAL_TABLE = "material";
	private static final String PROJECT_TABLE = "project";
	private static final String PROJECT_CATEGORY_TABLE = "project_category";
	private static final String STEP_TABLE = "step";

	public Project insertProject(Project project) {
		//@formatter:off
		String sql = "" + "INSERT INTO " + PROJECT_TABLE + " " + "(project_name, estimated_hours, actual_hours, difficulty, notes) " + "VALUES " + "(?, ?, ?, ?, ?)";
		//@formatter:on
		
		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			
			try (PreparedStatement statement = conn.prepareStatement(sql)) {
				setParameter(statement, 1, project.getProjectName(), String.class);
				setParameter(statement, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(statement, 3, project.getActualHours(), BigDecimal.class);
				setParameter(statement, 4, project.getDifficulty(), Integer.class);
				setParameter(statement, 5, project.getNotes(), String.class);
				
				statement.executeUpdate();
				
				Integer projectId = getLastInsertId(conn, PROJECT_TABLE);
				commitTransaction(conn);
				
				project.setProjectId(projectId);
				return project;
				
			}
			catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
		catch (SQLException eSQL) {
			 throw new DbException(eSQL);
		}
	}

	public List<Project> fetchAllProjects() {
		String sql = "SELECT * FROM " + PROJECT_TABLE + " ORDER BY project_name";
		
		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			
			try (PreparedStatement statement = conn.prepareStatement(sql)) {
				try(ResultSet result = statement.executeQuery()) {
					List<Project> projects = new LinkedList<>();
					
					while (result.next())
					{
						projects.add(extract(result, Project.class));
					}
					
					return projects;
				}
			}
			catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
		catch (SQLException eSQL) {
			throw new DbException(eSQL);
		}
	}

	public Optional<Project> fetchProjectById(Integer projectId) {
		String sql = ("SELECT * FROM " + PROJECT_TABLE + " WHERE project_id = ?");
		
		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			try {
				Project project = null;
				try (PreparedStatement statement = conn.prepareStatement(sql)) {
					setParameter(statement, 1, projectId, Integer.class);
					
					try (ResultSet results = statement.executeQuery()) {
						if (results.next()) {
							project = extract(results, Project.class);
						}
					}
				}
				if(Objects.nonNull(project)) {
					project.getMaterials().addAll(fetchMaterialsForProject(conn, projectId));
					project.getSteps().addAll(fetchStepsForProject(conn, projectId));
					project.getCategories().addAll(fetchCategoriesForProject(conn, projectId));
				}
				commitTransaction(conn);
				return Optional.ofNullable(project);
			}
			catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
		catch (SQLException eSQL) {
			throw new DbException(eSQL);
		}
	}

	private List<Category> fetchCategoriesForProject(Connection conn, Integer projectId) throws SQLException {
		String sql = "SELECT * FROM " + CATEGORY_TABLE + " c " + " JOIN " + PROJECT_CATEGORY_TABLE + " pc USING (category_id) "
					+ "WHERE project_id = ?";
		
		try (PreparedStatement statement = conn.prepareStatement(sql)) {
			setParameter(statement, 1, projectId, Integer.class);
			
			try (ResultSet results = statement.executeQuery()) {
				List<Category> categories = new LinkedList<>();
				
				while (results.next())
				{
					categories.add(extract(results, Category.class));
				}
				
				return categories;
			}
		}
	}

	private List<Step> fetchStepsForProject(Connection conn, Integer projectId) throws SQLException {
		
		String sql = "SELECT * FROM " + STEP_TABLE + " WHERE project_id = ?";
	
		try (PreparedStatement statement = conn.prepareStatement(sql)) {
			setParameter(statement, 1, projectId, Integer.class);
		
			try (ResultSet results = statement.executeQuery()) {
				List<Step> steps = new LinkedList<>();
			
				while (results.next())
				{
					steps.add(extract(results, Step.class));
				}
			
				return steps;
			}
		}

	}

	private List<Material> fetchMaterialsForProject(Connection conn, Integer projectId) throws SQLException {
		
		String sql = "SELECT * FROM " + MATERIAL_TABLE + " WHERE project_id = ?";
		
		try (PreparedStatement statement = conn.prepareStatement(sql)) {
			setParameter(statement, 1, projectId, Integer.class);
		
			try (ResultSet results = statement.executeQuery()) {
				List<Material> materials = new LinkedList<>();
			
				while (results.next())
				{
					materials.add(extract(results, Material.class));
				}
			
				return materials;
			}
		}
	}

	public boolean modifyProjectDetails(Project project) {
		String sql = "UPDATE " + PROJECT_TABLE + " SET " + "project_name = ?, " + "estimated_hours = ?, " 
					+ "actual_hours = ?, " + "difficulty = ?, " + "notes = ? " + "WHERE project_id = ?";
		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			
			try (PreparedStatement statement = conn.prepareStatement(sql)) {
				setParameter(statement, 1, project.getProjectName(), String.class);
				setParameter(statement, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(statement, 3, project.getActualHours(), BigDecimal.class);
				setParameter(statement, 4, project.getDifficulty(), Integer.class);
				setParameter(statement, 5, project.getNotes(), String.class);
				setParameter(statement, 6, project.getProjectId(), Integer.class);
				
				boolean isModified;
				if (statement.executeUpdate() == 1) {
					isModified = true;
				}
				else {
					isModified = false;
				}
				
				commitTransaction(conn);
				
				return isModified;
			}
			
			catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
		
		catch (SQLException eSQL) {
			 throw new DbException(eSQL);
		}
	}

	public boolean deleteProject(Integer projectId) {
		String sql = "DELETE FROM " + PROJECT_TABLE + " WHERE project_id = ?";
		
		try (Connection conn = DbConnection.getConnection()) {
			startTransaction(conn);
			
			try (PreparedStatement statement = conn.prepareStatement(sql)) {
				setParameter(statement, 1, projectId, Integer.class);
				
				boolean isDeleted;
				if (statement.executeUpdate() == 1) {
					isDeleted = true;
				}
				else {
					isDeleted = false;
				}
				
				commitTransaction(conn);
				return isDeleted;
			}
			
			catch (Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
		catch (SQLException eSQL) {
			throw new DbException(eSQL);
		}
	}
}
