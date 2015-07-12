package com.github.ambry.coordinator;

import com.github.ambry.clustermap.ClusterMap;
import com.github.ambry.clustermap.ReplicaId;
import com.github.ambry.commons.BlobId;
import com.github.ambry.messageformat.BlobProperties;
import com.github.ambry.messageformat.MessageFormatException;
import com.github.ambry.messageformat.MessageFormatFlags;
import com.github.ambry.messageformat.MessageFormatRecord;
import com.github.ambry.network.ConnectionPool;
import com.github.ambry.protocol.RequestOrResponse;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;


/**
 * Performs a get blob data operation by sending and receiving get requests until operation is complete or has failed.
 */
final public class GetBlobPropertiesOperation extends GetOperation {
  private BlobProperties blobProperties;
  private Logger logger = LoggerFactory.getLogger(getClass());

  public GetBlobPropertiesOperation(String datacenterName, ConnectionPool connectionPool, ExecutorService requesterPool,
      OperationContext oc, BlobId blobId, long operationTimeoutMs, ClusterMap clusterMap, ArrayList<String> sslEnabledColos)
      throws CoordinatorException {
    super(datacenterName, connectionPool, requesterPool, oc, blobId, operationTimeoutMs, clusterMap,
        MessageFormatFlags.BlobProperties, sslEnabledColos);
    this.blobProperties = null;
  }

  @Override
  protected OperationRequest makeOperationRequest(ReplicaId replicaId) {
    if(!sslEnabledColos.contains(replicaId.getDataNodeId().getDatacenterName())) {
      return new GetBlobPropertiesOperationRequest(connectionPool, responseQueue, context, blobId, replicaId,
          makeGetRequest(), clusterMap, this, false);
    }
    else{
      return new GetBlobPropertiesOperationRequest(connectionPool, responseQueue, context, blobId, replicaId,
          makeGetRequest(), clusterMap, this, true);
    }
  }

  public BlobProperties getBlobProperties()
      throws CoordinatorException {
    if (blobProperties != null) {
      return blobProperties;
    }
    CoordinatorException e = new CoordinatorException("GetBlobProperties has invalid return data.",
        CoordinatorError.UnexpectedInternalError);
    logger.error("blobProperties is null and should not be: {}", e);
    throw e;
  }

  public synchronized void setBlobProperties(BlobProperties blobProperties) {
    if (this.blobProperties == null) {
      this.blobProperties = blobProperties;
    }
  }
}

final class GetBlobPropertiesOperationRequest extends GetOperationRequest {
  private GetBlobPropertiesOperation getBlobPropertiesOperation;
  private Logger logger = LoggerFactory.getLogger(getClass());

  protected GetBlobPropertiesOperationRequest(ConnectionPool connectionPool,
      BlockingQueue<OperationResponse> responseQueue, OperationContext context, BlobId blobId, ReplicaId replicaId,
      RequestOrResponse request, ClusterMap clusterMap, GetBlobPropertiesOperation getBlobPropertiesOperation,
      boolean sslEnabled) {
    super(connectionPool, responseQueue, context, blobId, replicaId, request, clusterMap, sslEnabled);
    this.getBlobPropertiesOperation = getBlobPropertiesOperation;
    logger.trace("Created GetBlobPropertiesOperationRequest for " + replicaId);
  }

  @Override
  protected void markRequest() {
    CoordinatorMetrics.RequestMetrics metric =
        context.getCoordinatorMetrics().getRequestMetrics(replicaId.getDataNodeId());
    if (metric != null) {
      metric.getBlobPropertiesRequestRate.mark();
    }
  }

  @Override
  protected void updateRequest(long durationInMs) {
    CoordinatorMetrics.RequestMetrics metric =
        context.getCoordinatorMetrics().getRequestMetrics(replicaId.getDataNodeId());
    if (metric != null) {
      metric.getBlobPropertiesRequestLatencyInMs.update(durationInMs);
    }
  }

  @Override
  protected void deserializeBody(InputStream inputStream)
      throws IOException, MessageFormatException {
    getBlobPropertiesOperation.setBlobProperties(MessageFormatRecord.deserializeBlobProperties(inputStream));
  }
}

