package telran.students.repo;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.bson.Document;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import telran.students.dto.Mark;
import telran.students.dto.NameAvgScore;
import telran.students.dto.Student;
import telran.students.model.StudentDoc;

@Service
@Slf4j
@RequiredArgsConstructor

public class StudentAggregateRepoImpl implements StudentAggregateRepo {
	final MongoTemplate mongoTemplate;
	final int levelGoodScore = 80;
	
	@Override
	public List<Mark> aggregateStudentSubjectMarks(long id, String subject) {
		MatchOperation matchStudent = Aggregation.match(Criteria.where("id").is(id));
		UnwindOperation unwindOperation = Aggregation.unwind("marks");
		MatchOperation matchMarksSubject = Aggregation.match(Criteria.where("marks.subject").is(subject));
		ProjectionOperation projectionOperation = Aggregation.project("marks.score", "marks.date");
		Aggregation pipeLine = Aggregation.newAggregation(matchStudent, unwindOperation,
				matchMarksSubject, projectionOperation);
		var aggregationResult = mongoTemplate.aggregate(pipeLine, StudentDoc.class, Document.class);
		List<Document> listDocuments = aggregationResult.getMappedResults();
		log.debug("listDocuments: {}", listDocuments);
		List<Mark> result = listDocuments.stream()
				.map(d -> new Mark(subject, d.getDate("date").toInstant()
						.atZone(ZoneId.systemDefault()).toLocalDate(), d.getInteger("score"))).toList();
		return result;			
	}
	
	@Override
	public List<NameAvgScore> aggregateStudentAvgScoreGreater(int avgScoreThreshold) {
		UnwindOperation unwindOperation = Aggregation.unwind("marks");
		GroupOperation groupOperation = Aggregation.group("name").avg("marks.score").as("avgMark");
		MatchOperation matchOperation = Aggregation.match(Criteria.where("avgMark").gt(avgScoreThreshold));
		SortOperation sortOperation = Aggregation.sort(Direction.DESC, "avgMark");
		Aggregation pipeLine = Aggregation.newAggregation(unwindOperation, groupOperation, matchOperation, sortOperation);
		List<NameAvgScore> res = mongoTemplate.aggregate(pipeLine, StudentDoc.class, Document.class)
				.getMappedResults().stream().map(d -> new NameAvgScore(d.getString("_id"),
						d.getDouble("avgMark").intValue())).toList();		
		return res;
	}

	@Override
	public List<Mark> aggregateStudentMarksAtDates(long id, LocalDate from, LocalDate to) {
		MatchOperation matchStudent = Aggregation.match(Criteria.where("id").is(id));
		UnwindOperation unwindMarks = Aggregation.unwind("marks");
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("marks.date").gte(from), Criteria.where("marks.date").lte(to) );
		MatchOperation matchDates = Aggregation.match(criteria);
		ProjectionOperation projectionOperation = Aggregation.project("marks.score", "marks.date", "marks.subject");
		Aggregation pipeLine = Aggregation.newAggregation(matchStudent, unwindMarks, matchDates, projectionOperation);
		var aggregationResult = mongoTemplate.aggregate(pipeLine, StudentDoc.class, Document.class);
		List<Document> listDocuments = aggregationResult.getMappedResults();
		log.debug("list of documents: {}", listDocuments);
		List<Mark> result = listDocuments.stream().map(d -> new Mark(d.getString("subject"), 
				d.getDate("date").toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), 
				d.getInteger("score"))).toList();
		return result;
	}

	@Override
	public List<Student> aggregateBestStudents(int nStudents) {
		Criteria criteria = Criteria.where("marks.score").gt(levelGoodScore);
		UnwindOperation unwindMarks = Aggregation.unwind("marks");
		MatchOperation matchMarks = Aggregation.match(criteria);
		GroupOperation groupOperation = Aggregation.group("id", "name", "phone").count().as("markCount");
		SortOperation sortOperation = Aggregation.sort(Direction.DESC, "markCount");
		LimitOperation limitOperation = Aggregation.limit(nStudents);
		ProjectionOperation projectionOperation = Aggregation.project("id", "name", "phone");
		Aggregation pipeLine = Aggregation.newAggregation(unwindMarks, matchMarks, groupOperation, sortOperation, limitOperation, projectionOperation);
		var aggregationResult = mongoTemplate.aggregate(pipeLine, StudentDoc.class, Document.class);
		List<Document> listDocuments = aggregationResult.getMappedResults();
		log.debug("list of documents: {}", listDocuments);
		List<Student> result = listDocuments.stream()
				.map(d -> new Student(d.getLong("id"), d.getString("name"), d.getString("phone")) ).toList();		
		return result;
	}



}
