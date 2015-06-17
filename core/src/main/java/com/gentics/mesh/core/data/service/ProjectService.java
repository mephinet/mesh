package com.gentics.mesh.core.data.service;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gentics.mesh.core.Page;
import com.gentics.mesh.core.data.model.root.ProjectRoot;
import com.gentics.mesh.core.data.model.tinkerpop.MeshUser;
import com.gentics.mesh.core.data.model.tinkerpop.Project;
import com.gentics.mesh.paging.PagingInfo;
import com.gentics.mesh.util.InvalidArgumentException;
import com.gentics.mesh.util.TraversalHelper;

@Component
public class ProjectService extends AbstractMeshService {

	private static Logger log = LoggerFactory.getLogger(ProjectService.class);

	@Autowired
	protected UserService userService;

	public Project findByName(String projectName) {
		return fg.v().has("name", projectName).nextOrDefault(Project.class, null);
	}

	public Project findByUUID(String uuid) {
		return fg.v().has("uuid", uuid).nextOrDefault(Project.class, null);
	}

	public List<? extends Project> findAll() {
		return fg.v().has(Project.class).toListExplicit(Project.class);
	}

	public void deleteByName(String name) {
	}

	public Page<? extends Project> findAllVisible(MeshUser requestUser, PagingInfo pagingInfo) throws InvalidArgumentException {
		// @Query(value =
		// "MATCH (requestUser:User)-[:MEMBER_OF]->(group:Group)<-[:HAS_ROLE]-(role:Role)-[perm:HAS_PERMISSION]->(project:Project) where id(requestUser) = {0} and perm.`permissions-read` = true return project ORDER BY project.name",
		// countQuery =
		// "MATCH (requestUser:User)-[:MEMBER_OF]->(group:Group)<-[:HAS_ROLE]-(role:Role)-[perm:HAS_PERMISSION]->(project:Project) where id(requestUser) = {0} and perm.`permissions-read` = true return count(project)")
		// TODO check whether it is faster to use meshroot for starting the traversal
		return TraversalHelper.getPagedResult(fg.v().has(ProjectRoot.class), pagingInfo, Project.class);

	}

	public ProjectRoot findRoot() {
		return fg.v().nextOrDefault(ProjectRoot.class, null);
	}

	public Project create(String name) {
		Project project = fg.addFramedVertex(Project.class);
		project.setName(name);
		return project;
	}

	public ProjectRoot createRoot() {
		return fg.addFramedVertex(ProjectRoot.class);
	}

	public void delete(Project project) {
		project.getVertex().remove();
	}

}
