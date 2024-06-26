package com.traineeapp.repository;

import com.traineeapp.exception.RecordNotFoundException;
import com.traineeapp.model.Trainee;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TraineeRepositoryImpl implements TraineeRepository {
//    @Autowired
//    Connection connection;

    @Autowired
    ApplicationContext context;

    @Autowired
    Environment env;

    @Override
    public Trainee saveTrainee(Trainee trainee) {
        try {

            Connection connection = context.getBean(Connection.class);
            String query = "insert into trainees(trainee_name,email,location,dob) values(?,?,?,?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, trainee.getTraineeName());
            statement.setString(2, trainee.getEmail());
            statement.setString(3, trainee.getLocation());
            statement.setDate(4, Date.valueOf(trainee.getDob()));
            int rowCount = statement.executeUpdate();
            if (rowCount == 0) {
                throw new RuntimeException("Failed to Save");
            }
            connection.close();
            return getTraineeByName(trainee.getTraineeName()).orElse(null);
        } catch (SQLException | RecordNotFoundException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public Optional<Trainee> getTraineeById(int id) throws RecordNotFoundException {
        try {
            Connection connection = context.getBean(Connection.class);
            String query = "select * from trainees where id=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            ResultSet queryResult = statement.executeQuery();
            Optional<Trainee> trainee = getTraineeFromResult(queryResult);
            connection.close();
            return trainee;
        } catch (SQLException ex) {
            throw new RecordNotFoundException(ex.getMessage());
        }
    }

    @Override
    public Optional<Trainee> getTraineeByName(String traineeName) throws RecordNotFoundException {
        try {
            Connection connection = context.getBean(Connection.class);
            String query = "select * from trainees where trainee_name like ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, traineeName + "%");
            ResultSet queryResult = statement.executeQuery();
            Optional<Trainee> trainee = getTraineeFromResult(queryResult);
            return trainee;

        } catch (SQLException ex) {
            throw new RecordNotFoundException(ex.getMessage());
        }
    }

    @Override
    public List<Trainee> getAllTrainees() {
        try {
            Connection connection = context.getBean(Connection.class);
            String query = "select * from trainees";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet queryResult = statement.executeQuery();
            List<Trainee> trainees = getTraineesFromResult(queryResult);
            return trainees;
        } catch (SQLException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public void deleteTrainee(int id) throws RecordNotFoundException {
        try {
            Connection connection = context.getBean(Connection.class);
            String query = "delete from trainees where id=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            int rowCount = statement.executeUpdate();
            if (rowCount == 0) {
                throw new RuntimeException("Failed to Delete");
            }
        } catch (SQLException ex) {
            throw new RecordNotFoundException(ex.getMessage());
        }
    }

    @Override
    public Trainee updateTrainee(int id, Trainee trainee) throws RecordNotFoundException {
        try {
            Connection connection = context.getBean(Connection.class);
            String query = "update trainees set trainee_name=?, email=?, location=?, dob=?, laptop_id=?, team_lead=? where id=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, trainee.getTraineeName());
            statement.setString(2, trainee.getEmail());
            statement.setString(3, trainee.getLocation());
            statement.setDate(4, Date.valueOf(trainee.getDob()));
            statement.setInt(5, trainee.getLaptopId());
            statement.setInt(6, trainee.getTeamLead());
            statement.setInt(7, id);
            int rowCount = statement.executeUpdate();
            if (rowCount == 0) {
                throw new RuntimeException("Failed to update");
            }
            return trainee;
        } catch (SQLException ex) {
            throw new RecordNotFoundException(ex.getMessage());
        }
    }

    private Optional<Trainee> getTraineeFromResult(ResultSet resultSet) throws SQLException {
        // ResultSetMetaData metaData = resultSet.getMetaData();

        if (resultSet.next()) {
            Trainee trainee = processResultSet(resultSet);
            return Optional.of(trainee);
        }
        return Optional.empty();
    }

    private Trainee processResultSet(ResultSet resultSet) throws SQLException {
        Trainee trainee = new Trainee();
        trainee.setId(resultSet.getInt("id"));
        trainee.setTraineeName(resultSet.getString("trainee_name"));
        trainee.setEmail(resultSet.getString("email"));
        Date dob = resultSet.getDate("dob");
        if (dob != null) {
            trainee.setDob(dob.toLocalDate());
        }
        trainee.setLocation(resultSet.getString("location"));
        trainee.setLaptopId(resultSet.getInt("laptop_id"));
        trainee.setTeamLead(resultSet.getInt("team_lead"));
        return trainee;
    }

    private List<Trainee> getTraineesFromResult(ResultSet resultSet) throws SQLException {
        // ResultSetMetaData metaData = resultSet.getMetaData();
        List<Trainee> trainees = new ArrayList<>();
        while (resultSet.next()) {
            Trainee trainee = processResultSet(resultSet);
            trainees.add(trainee);
        }
        return trainees;
    }

}
