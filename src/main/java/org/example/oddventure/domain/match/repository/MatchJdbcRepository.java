package org.example.oddventure.domain.match.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.domain.match.entity.Match;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MatchJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public void saveAllMatches(List<Match> matches) {
        String sql =
                "INSERT INTO `match`(fetch_id, match_name, team_a, team_b, start_time, status, total_amounta, total_amountb) "
                        + "VALUES (?, ?, ?, ?, ?, 'SCHEDULED', 1000, 1000)";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Match match = matches.get(i);
                ps.setLong(1, match.getFetchId());
                ps.setString(2, match.getMatchName());
                ps.setString(3, match.getTeamA());
                ps.setString(4, match.getTeamB());
                ps.setTimestamp(5, Timestamp.valueOf(match.getStartTime()));
            }

            @Override
            public int getBatchSize() {
                return matches.size();
            }
        });
    }
}
