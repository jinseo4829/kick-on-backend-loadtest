package kr.kickon.api.domain.shorts;

import kr.kickon.api.global.common.entities.Shorts;
import kr.kickon.api.global.common.enums.ShortsType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShortsRepository extends JpaRepository<Shorts, Long>, QuerydslPredicateExecutor<Shorts> {

  Shorts findByReferencePkAndType(Long referncePk, ShortsType shortsType);

    @Query(value = """
        SELECT s.pk AS shortsPk,
               CASE
                   WHEN s.type = 'AWS_FILE'
                        THEN CONCAT('https://kickon-files-bucket.s3.ap-northeast-2.amazonaws.com/', afr.s3_key)
                   ELSE REPLACE(el.url, 'youtube.com/embed/', 'youtube.com/watch?v=')
               END AS videoUrl,
               COALESCE(afr.used_in, el.used_in) AS usedIn,
               COALESCE(b.pk, n.pk) AS referencePk,
               COALESCE(b.title, n.title) AS title,
               COUNT(DISTINCT bvh.pk) + COUNT(DISTINCT nvh.pk) AS totalViewCount,
               COUNT(DISTINCT bk.pk) + COUNT(DISTINCT nk.pk) AS totalKickCount,
               (SELECT COUNT(*)
                FROM boardViewHistory bv
                WHERE bv.board_pk = b.pk
                  AND bv.created_at > NOW() - INTERVAL 48 HOUR) +
               (SELECT COUNT(*)
                FROM newsViewHistory nv
                WHERE nv.news_pk = n.pk
                  AND nv.created_at > NOW() - INTERVAL 48 HOUR) AS recentViewCount,
               (SELECT COUNT(*)
                FROM boardKick bk2
                WHERE bk2.board_pk = b.pk
                  AND bk2.status = 'ACTIVATED'
                  AND bk2.created_at > NOW() - INTERVAL 48 HOUR) +
               (SELECT COUNT(*)
                FROM newsKick nk2
                WHERE nk2.news_pk = n.pk
                  AND nk2.status = 'ACTIVATED'
                  AND nk2.created_at > NOW() - INTERVAL 48 HOUR) AS recentKickCount,
               s.created_at AS createdAt
        FROM shorts s
        LEFT JOIN awsFileReference afr ON (s.type = 'AWS_FILE' AND s.reference_pk = afr.pk)
        LEFT JOIN embeddedLink el ON (s.type = 'EMBEDDED_LINK' AND s.reference_pk = el.pk)
        LEFT JOIN board b ON (afr.reference_pk = b.pk OR el.reference_pk = b.pk)
        LEFT JOIN news n ON (afr.reference_pk = n.pk OR el.reference_pk = n.pk)
        LEFT JOIN boardViewHistory bvh ON b.pk = bvh.board_pk
        LEFT JOIN boardKick bk ON b.pk = bk.board_pk AND bk.status = 'ACTIVATED'
        LEFT JOIN newsViewHistory nvh ON n.pk = nvh.news_pk
        LEFT JOIN newsKick nk ON n.pk = nk.news_pk AND nk.status = 'ACTIVATED'
        WHERE s.status = 'ACTIVATED'
        GROUP BY s.pk, afr.s3_key, el.url, afr.used_in, el.used_in, b.pk, n.pk, b.title, n.title, s.created_at
        ORDER BY recentViewCount DESC, recentKickCount DESC, s.created_at DESC
        LIMIT 4
        """, nativeQuery = true)
    List<Object[]> findTop4ShortsByPopularity();

    @Query(value = """
        SELECT s.pk AS shortsPk,
               CASE
                   WHEN s.type = 'AWS_FILE'
                        THEN CONCAT('https://kickon-files-bucket.s3.ap-northeast-2.amazonaws.com/', afr.s3_key)
                   ELSE REPLACE(el.url, 'youtube.com/embed/', 'youtube.com/watch?v=')
               END AS videoUrl,
               COALESCE(afr.used_in, el.used_in) AS usedIn,
               COALESCE(b.pk, n.pk) AS referencePk,
               COALESCE(b.title, n.title) AS title,
               COUNT(DISTINCT bvh.pk) + COUNT(DISTINCT nvh.pk) AS totalViewCount,
               COUNT(DISTINCT bk.pk) + COUNT(DISTINCT nk.pk) AS totalKickCount,
               (SELECT COUNT(*)
                FROM boardViewHistory bv
                WHERE bv.board_pk = b.pk
                  AND bv.created_at > NOW() - INTERVAL 48 HOUR) +
               (SELECT COUNT(*)
                FROM newsViewHistory nv
                WHERE nv.news_pk = n.pk
                  AND nv.created_at > NOW() - INTERVAL 48 HOUR) AS recentViewCount,
               (SELECT COUNT(*)
                FROM boardKick bk2
                WHERE bk2.board_pk = b.pk
                  AND bk2.status = 'ACTIVATED'
                  AND bk2.created_at > NOW() - INTERVAL 48 HOUR) +
               (SELECT COUNT(*)
                FROM newsKick nk2
                WHERE nk2.news_pk = n.pk
                  AND nk2.status = 'ACTIVATED'
                  AND nk2.created_at > NOW() - INTERVAL 48 HOUR) AS recentKickCount,
               s.created_at AS createdAt
        FROM shorts s
        LEFT JOIN awsFileReference afr ON (s.type = 'AWS_FILE' AND s.reference_pk = afr.pk)
        LEFT JOIN embeddedLink el ON (s.type = 'EMBEDDED_LINK' AND s.reference_pk = el.pk)
        LEFT JOIN board b ON (afr.reference_pk = b.pk OR el.reference_pk = b.pk)
        LEFT JOIN news n ON (afr.reference_pk = n.pk OR el.reference_pk = n.pk)
        LEFT JOIN boardViewHistory bvh ON b.pk = bvh.board_pk
        LEFT JOIN boardKick bk ON b.pk = bk.board_pk AND bk.status = 'ACTIVATED'
        LEFT JOIN newsViewHistory nvh ON n.pk = nvh.news_pk
        LEFT JOIN newsKick nk ON n.pk = nk.news_pk AND nk.status = 'ACTIVATED'
        WHERE s.status = 'ACTIVATED'
        GROUP BY s.pk, afr.s3_key, el.url, afr.used_in, el.used_in, b.pk, n.pk, b.title, n.title, s.created_at
        """, nativeQuery = true)
    List<Object[]> findShorts();

        @Query(value = """
        SELECT s.pk AS shortsPk,
               CASE
                   WHEN s.type = 'AWS_FILE'
                        THEN CONCAT('https://kickon-files-bucket.s3.ap-northeast-2.amazonaws.com/', afr.s3_key)
                   ELSE REPLACE(el.url, 'youtube.com/embed/', 'youtube.com/watch?v=')
               END AS videoUrl,
               COALESCE(afr.used_in, el.used_in) AS usedIn,
               b.pk AS referencePk,
               b.title AS title,
               (SELECT COUNT(*) 
                  FROM boardViewHistory bv 
                 WHERE bv.board_pk = b.pk) AS totalViewCount,
               (SELECT COUNT(*) 
                  FROM boardKick bk 
                 WHERE bk.board_pk = b.pk
                   AND bk.status = 'ACTIVATED') AS totalKickCount,
               (SELECT COUNT(*) 
                  FROM boardViewHistory bv 
                 WHERE bv.board_pk = b.pk 
                   AND bv.created_at > NOW() - INTERVAL 48 HOUR) AS recentViewCount,
               (SELECT COUNT(*) 
                  FROM boardKick bk 
                 WHERE bk.board_pk = b.pk
                   AND bk.status = 'ACTIVATED'
                   AND bk.created_at > NOW() - INTERVAL 48 HOUR) AS recentKickCount,
               (SELECT COUNT(*) 
                  FROM boardReply br
                 WHERE br.board_pk = b.pk
                   AND br.status = 'ACTIVATED') AS totalReplyCount,
               s.created_at AS createdAt,
               u.pk AS userPk,
               u.nickname AS userNickname,
               u.profile_image_url AS profileImgaeUrl,
               EXISTS (
                  SELECT 1
                  FROM teamReporter tr
                   WHERE tr.user_pk = u.pk
               ) AS isReporter,
               EXISTS (
                  SELECT 1 FROM boardKick bk
                   WHERE bk.board_pk = b.pk
                     AND bk.status = 'ACTIVATED'
                     AND bk.user_pk = :userPk
               ) AS isKicked
        FROM shorts s
        LEFT JOIN awsFileReference afr ON (s.type = 'AWS_FILE' AND s.reference_pk = afr.pk)
        LEFT JOIN embeddedLink el ON (s.type = 'EMBEDDED_LINK' AND s.reference_pk = el.pk)
        LEFT JOIN board b ON (afr.reference_pk = b.pk OR el.reference_pk = b.pk)
        LEFT JOIN user u ON b.user_pk = u.pk
        WHERE s.status = 'ACTIVATED'
          AND s.pk = :shortsPk
        """, nativeQuery = true)
        Object[] findBoardShortsDetail(@Param("shortsPk") Long shortsPk, @Param("userPk") Long userPk);


        @Query(value = """
        SELECT s.pk AS shortsPk,
               CASE
                   WHEN s.type = 'AWS_FILE'
                        THEN CONCAT('https://kickon-files-bucket.s3.ap-northeast-2.amazonaws.com/', afr.s3_key)
                   ELSE REPLACE(el.url, 'youtube.com/embed/', 'youtube.com/watch?v=')
               END AS videoUrl,
               COALESCE(afr.used_in, el.used_in) AS usedIn,
               n.pk AS referencePk,
               n.title AS title,
               (SELECT COUNT(*) 
                  FROM newsViewHistory nv 
                 WHERE nv.news_pk = n.pk) AS totalViewCount,
               (SELECT COUNT(*) 
                  FROM newsKick nk 
                 WHERE nk.news_pk = n.pk
                   AND nk.status = 'ACTIVATED') AS totalKickCount,
               (SELECT COUNT(*) 
                  FROM newsViewHistory nv 
                 WHERE nv.news_pk = n.pk 
                   AND nv.created_at > NOW() - INTERVAL 48 HOUR) AS recentViewCount,
               (SELECT COUNT(*) 
                  FROM newsKick nk 
                 WHERE nk.news_pk = n.pk
                   AND nk.status = 'ACTIVATED'
                   AND nk.created_at > NOW() - INTERVAL 48 HOUR) AS recentKickCount,
               (SELECT COUNT(*) 
                  FROM newsReply nr
                 WHERE nr.news_pk = n.pk
                   AND nr.status = 'ACTIVATED') AS totalReplyCount,
               s.created_at AS createdAt,
               u.pk AS userPk,
               u.nickname AS userNickname,
               u.profile_image_url AS profileImgaeUrl,
               EXISTS (
                  SELECT 1
                  FROM teamReporter tr
                   WHERE tr.user_pk = u.pk
               ) AS isReporter,
               EXISTS (
                  SELECT 1 FROM newsKick nk
                   WHERE nk.news_pk = n.pk
                     AND nk.status = 'ACTIVATED'
                     AND nk.user_pk = :userPk
               ) AS isKicked
        FROM shorts s
        LEFT JOIN awsFileReference afr ON (s.type = 'AWS_FILE' AND s.reference_pk = afr.pk)
        LEFT JOIN embeddedLink el ON (s.type = 'EMBEDDED_LINK' AND s.reference_pk = el.pk)
        LEFT JOIN news n ON (afr.reference_pk = n.pk OR el.reference_pk = n.pk)
        LEFT JOIN user u ON n.user_pk = u.pk
        WHERE s.status = 'ACTIVATED'
          AND s.pk = :shortsPk
        """, nativeQuery = true)
        Object[] findNewsShortsDetail(@Param("shortsPk") Long shortsPk, @Param("userPk") Long userPk);

}
