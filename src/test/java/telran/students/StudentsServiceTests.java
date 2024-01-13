package telran.students;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.MongoTransactionManager;

import telran.exceptions.NotFoundException;
import telran.students.dto.Mark;
import telran.students.dto.NameAvgScore;
import telran.students.dto.Student;
import telran.students.repo.StudentRepo;
import telran.students.service.StudentsService;
@SpringBootTest
class StudentsServiceTests {
	@Autowired
StudentsService studentsService;
	@Autowired
	DbTestCreation dbCreation;
	@Autowired
StudentRepo studentRepo;
	@MockBean
	MongoTransactionManager transactionManager;
	@BeforeEach
	void setUp() {
		dbCreation.createDB();
	}
	
	@Test
	void getMarksTest() {
		Mark[] marksActual = studentsService.getMarks(1).toArray(Mark[]::new);
		Mark[] marksExpected = dbCreation.getStudentMarks(1);
		assertArrayEquals(marksExpected, marksActual);
		assertThrowsExactly(NotFoundException.class, () -> studentsService.getMarks(10000000));
	}
	@Test
	void removeStudentTest() {
		assertNotNull(studentRepo.findById(1l).orElse(null));
		assertEquals(dbCreation.getStudent(1), studentsService.removeStudent(1));
		assertNull(studentRepo.findById(1l).orElse(null));
		assertThrowsExactly(NotFoundException.class, ()->studentsService.removeStudent(1));
	}
	@Test
	void addMarkTest() {
		List<Mark> marksStudent2Expected = new ArrayList<>(Arrays.asList(dbCreation.getStudentMarks(2)));
		Mark mark = new Mark("Java", LocalDate.now(), 100);
		marksStudent2Expected.add(mark);
		assertIterableEquals(marksStudent2Expected, studentsService.addMark(2, mark));
		assertIterableEquals(marksStudent2Expected, studentsService.getMarks(2));
		assertThrowsExactly(NotFoundException.class, () -> studentsService.addMark(0, mark));
	}
	
	@Test
	void updatePhoneTest() {
		Student student3 = dbCreation.getStudent(3);
		String newPhone = "055-5555555";
		Student expected = new Student(student3.id(), student3.name(), newPhone);
		assertEquals(expected, studentsService.updatePhone(3, newPhone));
		Student actual = studentRepo.findById(3l).orElseThrow().build();
		assertEquals(expected, actual);
		assertThrowsExactly(NotFoundException.class, () -> studentsService.updatePhone(0, newPhone));
	}
	@Test
	void addStudentTest() {
		Student studentExisting = dbCreation.getStudent(4);
		Student newStudent = new Student(-1l, "Vasya", "111111111");
		assertEquals(newStudent, studentsService.addStudent(newStudent));
		Student actual = studentRepo.findById(-1l).orElseThrow().build();
		assertEquals(newStudent, actual);
		assertThrowsExactly(IllegalStateException.class,
				()-> studentsService.addStudent(studentExisting));
		assertThrowsExactly(IllegalStateException.class,
				()-> studentsService.addStudent(newStudent));
	}
	@Test
	void getStudentPhoneTest() {
		Student student2 = dbCreation.getStudent(2);
		assertEquals(student2, studentsService.getStudentByPhone(DbTestCreation.PHONE_2));
		assertNull(studentsService.getStudentByPhone("kuku"));
	}
	@Test
	void getStudentsPhonePrefixTest() {
		List<Student> expected = List.of(dbCreation.getStudent(2));
		String phonePrefix = DbTestCreation.PHONE_2.substring(0, 3);
		List<Student> actual = studentsService.getStudentsByPhonePrefix(phonePrefix);
		assertIterableEquals(expected, actual);
		assertTrue(studentsService.getStudentsByPhonePrefix("kuku").isEmpty());
	}
	@Test
	void getGoodStudentsTest() {
		List<Student> expected = List.of(dbCreation.getStudent(4), dbCreation.getStudent(6));
		List<Student> actual = studentsService.getStudentsAllGoodMarks(70);
		assertIterableEquals(expected, actual);
		assertTrue(studentsService.getStudentsAllGoodMarks(100).isEmpty());
	}
	@Test
	void getStudentsFewMarksTest() {
		List<Student> expected = List.of(dbCreation.getStudent(2), dbCreation.getStudent(7));
		List<Student> actual = studentsService.getStudentsFewMarks(2);
		assertIterableEquals(expected, actual);
		assertTrue(studentsService.getStudentsFewMarks(0).isEmpty());
	}
	@Test
	void getGoodStudentsSubjectTest() {
		List<Student> expected =  new ArrayList<>();
		expected.add(dbCreation.getStudent(6));
		List<Student> actual = studentsService.getStudentsAllGoodMarksSubject(DbTestCreation.SUBJECT_1, 85);
		assertEquals(expected, actual);
		assertIterableEquals(expected, actual);
		
		actual = studentsService.getStudentsAllGoodMarksSubject(DbTestCreation.SUBJECT_1, 75);		
		expected.add(0, dbCreation.getStudent(3));
		expected.add(0, dbCreation.getStudent(1));
		assertEquals(expected, actual);
		assertIterableEquals(expected, actual);
	}
	@Test
	void getStudentsMarksAmountBetween() {
		List<Student> expected = List.of(dbCreation.students[0], dbCreation.students[2], dbCreation.students[3], dbCreation.students[4]);
		List<Student> actual = studentsService.getStudentsMarksAmountBetween(2, 3);
		assertIterableEquals(expected, actual);
	}
	@Test
	void getStudentSubjectMarksOnClientFilterTest() {
		List<Mark> expection = List.of(dbCreation.marks[0][0], dbCreation.marks[0][1]);		
		List<Mark> actual = studentsService.getStudentSubjectMarksOnClientFilter(1, DbTestCreation.SUBJECT_1);
		assertIterableEquals(expection, actual);
		assertThrowsExactly(NotFoundException.class, () -> studentsService.getStudentSubjectMarksOnClientFilter(1000, DbTestCreation.SUBJECT_1));		
		assertTrue(studentsService.getStudentSubjectMarks(2, DbTestCreation.SUBJECT_2).isEmpty());
	}
	@Test
	void getStudentSubjectMarksTest() {
		List<Mark> expection = List.of(dbCreation.marks[0][0], dbCreation.marks[0][1]);		
		List<Mark> actual = studentsService.getStudentSubjectMarks(1, DbTestCreation.SUBJECT_1);
		assertIterableEquals(expection, actual);
		assertThrowsExactly(NotFoundException.class, () -> studentsService.getStudentSubjectMarks(1000, DbTestCreation.SUBJECT_1));		
		assertTrue(studentsService.getStudentSubjectMarks(2, DbTestCreation.SUBJECT_2).isEmpty());
	}
	@Test
	void getStudentAvgScoreGreater() {
		List<NameAvgScore> expected = List.of(new NameAvgScore(DbTestCreation.NAME_6, 100),
				new NameAvgScore(DbTestCreation.NAME_4, 93));
		List<NameAvgScore> actual = studentsService.getStudentAvgScoreGreater(90);
		assertIterableEquals(expected, actual);
	}
	@Test
	void getStudentMarksAtDatesTest() {
		List<Mark> expected = List.of(dbCreation.marks[5][0], dbCreation.marks[5][1]);
		List<Mark> actual = studentsService.getStudentMarksAtDates(DbTestCreation.ID_6, DbTestCreation.DATE_1, DbTestCreation.DATE_2);
		assertIterableEquals(expected, actual);
		assertTrue(studentsService.getStudentMarksAtDates(DbTestCreation.ID_2, DbTestCreation.DATE_2, DbTestCreation.DATE_3).isEmpty());
	}
	@Test
	void getBestStudentsTest() {
		List<Student> expected = List.of(dbCreation.students[5], dbCreation.students[3], dbCreation.students[0]);
		List<Student> actual = studentsService.getBestStudents(3);
		assertIterableEquals(expected, actual);
	}
	@Test
	void getWorstStudentsTest() {
		//TODO
	}

}
