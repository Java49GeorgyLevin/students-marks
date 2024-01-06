package telran.students.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import telran.students.model.StudentDoc;

public interface StudentRepo extends MongoRepository<StudentDoc, Long> {
	@Query(value = "{id:?0}", fields = "{mark:1}")
	StudentDoc findStudentMarks(long id);
	

	@Query(value="{id:?0}", fields = "{id:1, name:1, phone:1, marks: 0}")
	StudentDoc findStudentNoMarks(long id);

}
