package com.lostsidewalk.buffy.engine;

import com.lostsidewalk.buffy.DataAccessException;
import com.lostsidewalk.buffy.DataUpdateException;
import com.lostsidewalk.buffy.PostPublisher;
import com.lostsidewalk.buffy.engine.audit.ErrorLogService;
import com.lostsidewalk.buffy.post.PostArchiver;
import com.lostsidewalk.buffy.post.StagingPost;
import com.lostsidewalk.buffy.post.StagingPostDao;
import com.lostsidewalk.buffy.queue.QueueDefinition;
import com.lostsidewalk.buffy.queue.QueueDefinitionDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.apache.commons.collections4.CollectionUtils.size;
import static org.apache.commons.lang3.time.FastDateFormat.MEDIUM;
import static org.apache.commons.lang3.time.FastDateFormat.getDateTimeInstance;

@Slf4j
@Configuration
public class EngineConfig {

    @Autowired
    PostArchiver postArchiver;

    @Autowired
    PostPublisher postPublisher;

    @Autowired
    StagingPostDao stagingPostDao;

    @Autowired
    QueueDefinitionDao queueDefinitionDao;
    //
    // perform the archival process once per minute
    //
    @Scheduled(fixedRateString = "1", timeUnit = MINUTES)
//    @Transactional
    public final void doArchival() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.info("Archival process starting at {}", getDateTimeInstance(MEDIUM, MEDIUM).format(stopWatch.getStartTime()));
        try {
            // (1) the archiver returns staging posts that it has archived
            List<StagingPost> archivedPosts = postArchiver.markExpiredPostsForArchive();
            // (2) depublish the archived posts in all queues by by grouping the archived posts by queueId,
            //     then running the publisher on each group of posts
            depublish(
                    groupByQueueId(archivedPosts)
            );
            stopWatch.stop();
            long postArchiveDuration = stopWatch.getTime(MILLISECONDS);
            log.info("Archival process completed at {}, postsArchived={}, postArchiveDuration={}",
                    getDateTimeInstance(MEDIUM, MEDIUM).format(stopWatch.getStopTime()),
                    size(archivedPosts),
                    postArchiveDuration);
        } catch (DataAccessException e) {
            log.error("The archive process failed due to: {}", e.getMessage());
            ErrorLogService.logDataAccessException(new Date(), e);
        } catch (DataUpdateException e) {
            log.error("The archive process failed due to: {}", e.getMessage());
            ErrorLogService.logDataUpdateException(new Date(), e);
        }
    }

    private Map<QueueDefinition, List<StagingPost>> groupByQueueId(Iterable<? extends StagingPost> archivedPosts) throws DataAccessException {
        Map<QueueDefinition, List<StagingPost>> archivedPostsByQueueId = new HashMap<>(256);
        for (StagingPost stagingPost : archivedPosts) {
            long queueId = stagingPost.getQueueId();
            QueueDefinition queueDefinition = queueDefinitionDao.findByQueueId(stagingPost.getUsername(), queueId);
            if (!archivedPostsByQueueId.containsKey(queueDefinition)) {
                archivedPostsByQueueId.put(queueDefinition, new ArrayList<>(256));
            }
            archivedPostsByQueueId.get(queueDefinition).add(stagingPost);
        }
        return archivedPostsByQueueId;
    }

    private void depublish(Map<QueueDefinition, List<StagingPost>> archivedPostsByQueueId) throws DataAccessException, DataUpdateException {
        for (Map.Entry<QueueDefinition, List<StagingPost>> entry : archivedPostsByQueueId.entrySet()) {
            QueueDefinition queueDefinition = entry.getKey();
            List<StagingPost> stagingPosts = entry.getValue();
            postPublisher.publishFeed(queueDefinition.getUsername(), queueDefinition.getId(), stagingPosts);
        }
    }

    @Override
    public final String toString() {
        return "EngineConfig{" +
                "postArchiver=" + postArchiver +
                ", postPublisher=" + postPublisher +
                ", stagingPostDao=" + stagingPostDao +
                ", queueDefinitionDao=" + queueDefinitionDao +
                '}';
    }
}
