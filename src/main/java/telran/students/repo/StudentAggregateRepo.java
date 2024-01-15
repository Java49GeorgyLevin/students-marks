package telran.students.repo;

import java.time.LocalDate;
import java.util.List;

import telran.students.dto.Mark;
import telran.students.dto.NameAvgScore;
import telran.students.dto.Student;

public interface StudentAggregateRepo {
	List<Mark> aggregateStudentSubjectMarks(long id, String subject);
	
	List<NameAvgScore> aggregateStudentAvgScoreGreater(int avgScoreThreshold);
	
	List<Mark> aggregateStudentMarksAtDates(long id, LocalDate from, LocalDate to);
	
	List<Student> aggregateBestStudents(int nStudents);

}
