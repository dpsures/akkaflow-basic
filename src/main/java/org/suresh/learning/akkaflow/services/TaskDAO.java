package org.suresh.learning.akkaflow.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.suresh.learning.akkaflow.beans.Task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

@Repository
public class TaskDAO {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public long createTask(final Task task) {

		KeyHolder holder = new GeneratedKeyHolder();

		jdbcTemplate.update(new PreparedStatementCreator() {

			@Override
			public PreparedStatement createPreparedStatement(
					Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement("INSERT INTO tasks (payload, updated" +
								") VALUES(?, NOW())", Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, task.getPayload());
				return ps;
			}
		}, holder);

		return holder.getKey().longValue();
	}
}
