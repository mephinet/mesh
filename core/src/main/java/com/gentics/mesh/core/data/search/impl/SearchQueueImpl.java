package com.gentics.mesh.core.data.search.impl;

import static com.gentics.mesh.core.data.relationship.GraphRelationships.HAS_BATCH;
import static com.gentics.mesh.core.data.search.SearchQueueBatch.BATCH_ID_PROPERTY_KEY;
import static com.gentics.mesh.core.data.search.SearchQueueEntryAction.CREATE_ACTION;

import java.util.concurrent.locks.ReentrantLock;

import com.gentics.mesh.cli.BootstrapInitializer;
import com.gentics.mesh.core.data.Group;
import com.gentics.mesh.core.data.Project;
import com.gentics.mesh.core.data.Role;
import com.gentics.mesh.core.data.SchemaContainer;
import com.gentics.mesh.core.data.Tag;
import com.gentics.mesh.core.data.TagFamily;
import com.gentics.mesh.core.data.User;
import com.gentics.mesh.core.data.generic.MeshVertexImpl;
import com.gentics.mesh.core.data.node.Node;
import com.gentics.mesh.core.data.search.SearchQueue;
import com.gentics.mesh.core.data.search.SearchQueueBatch;
import com.gentics.mesh.graphdb.Trx;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class SearchQueueImpl extends MeshVertexImpl implements SearchQueue {

	private static final Logger log = LoggerFactory.getLogger(SearchQueueImpl.class);

	/** Lock held by take, poll, etc */
	private final ReentrantLock takeLock = new ReentrantLock();

	@Override
	public void addBatch(SearchQueueBatch batch) {
		setLinkOutTo(batch.getImpl(), HAS_BATCH);
	}

	@Override
	public SearchQueueBatch take() throws InterruptedException {
		SearchQueueBatch entry;
		final ReentrantLock takeLock = this.takeLock;
		takeLock.lockInterruptibly();
		try {
			entry = out(HAS_BATCH).nextOrDefault(SearchQueueBatchImpl.class, null);
			if (entry != null) {
				unlinkOut(entry.getImpl(), HAS_BATCH);
				return entry;
			} else {
				return null;
			}

		} finally {
			takeLock.unlock();
		}

	}

	@Override
	public SearchQueueBatch take(String batchId) {
		SearchQueueBatch entry;
		final ReentrantLock takeLock = this.takeLock;
		try {
			takeLock.lockInterruptibly();
			try {
				entry = out(HAS_BATCH).has(BATCH_ID_PROPERTY_KEY).nextOrDefault(SearchQueueBatchImpl.class, null);
				if (entry != null) {
					unlinkOut(entry.getImpl(), HAS_BATCH);
					return entry;
				} else {
					return null;
				}
			} finally {
				takeLock.unlock();
			}
		} catch (InterruptedException e) {
			// TODO handle InterruptedException
			e.printStackTrace();
		}
		return null;

	}

	@Override
	public SearchQueueBatch createBatch(String batchId) {
		SearchQueueBatch batch = getGraph().addFramedVertex(SearchQueueBatchImpl.class);
		batch.setBatchId(batchId);
		addBatch(batch);
		return batch;
	}

	@Override
	public long getSize() {
		return out(HAS_BATCH).count();
	}

	@Override
	public void addFullIndex() {
		BootstrapInitializer boot = BootstrapInitializer.getBoot();
		SearchQueue searchQueue = boot.meshRoot().getSearchQueue();
		SearchQueueBatch batch = searchQueue.createBatch("0");
		for (Node node : boot.nodeRoot().findAll()) {
			batch.addEntry(node, CREATE_ACTION);
		}
		for (Project project : boot.projectRoot().findAll()) {
			batch.addEntry(project, CREATE_ACTION);
		}
		for (User user : boot.userRoot().findAll()) {
			batch.addEntry(user, CREATE_ACTION);
		}
		for (Role role : boot.roleRoot().findAll()) {
			batch.addEntry(role, CREATE_ACTION);
		}
		for (Group group : boot.groupRoot().findAll()) {
			batch.addEntry(group, CREATE_ACTION);
		}
		for (Tag tag : boot.tagRoot().findAll()) {
			batch.addEntry(tag, CREATE_ACTION);
		}
		for (TagFamily tagFamily : boot.tagFamilyRoot().findAll()) {
			batch.addEntry(tagFamily, CREATE_ACTION);
		}
		for (SchemaContainer schema : boot.schemaContainerRoot().findAll()) {
			batch.addEntry(schema, CREATE_ACTION);
		}
		// TODO add support for microschemas
		// for (Microschema microschema : boot.microschemaContainerRoot().findAll()) {
		// searchQueue.put(microschema, CREATE_ACTION);
		// }
		if (log.isDebugEnabled()) {
			log.debug("Search Queue size:" + searchQueue.getSize());
		}

	}

	@Override
	public void processAll() {
		// TODO Auto-generated method stub

	}

}
