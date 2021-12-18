package com.first.demo;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.springframework.data.rest.core.config.Projection;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor

class Student {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String name;
	private String email;
	private Date birthday;
	@ManyToOne
	private Laboratory laboratory;
}

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
class Laboratory {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String contact;
	@OneToMany(mappedBy = "laboratory")
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private Collection<Student> students;
}

@RepositoryRestResource
interface LaboratoryRepository extends JpaRepository<Laboratory, Long> {

}

@Projection(name = "p1", types = Student.class)
interface studentProjection {
	public String getEmail();

	public String getName();

	public String getLaboratory;
}

@RepositoryRestResource
interface StudentRepository extends JpaRepository<Student, Long> {
	@RestResource(path = "/byName")
	public List<Student> findByNameContains(@Param(value = "mc") String mc);
}

@RestController
@RequestMapping("/api")
class ScolarityRestController {
	@Autowired
	private StudentRepository studentRepository;
	@Autowired
	private LaboratoryRepository laboratoryRepository;

	// cette fonction pour selectionner tous les students
	@GetMapping("/students")
	public List<Student> Students() {
		return studentRepository.findAll();
	}

	// select one student
	@GetMapping("/students/{id}")
	public Student getOne(@PathVariable(name = "id") Long id) {
		return studentRepository.findById(id).get();
	}

	// save new student
	@PostMapping("/students")
	public Student save(@RequestBody Student student) {
		if(student.getLaboratory().getId() == null){
			Laboratory laboratory=laboratoryRepository.save(student.getLaboratory());
			student.setLaboratory(laboratory);
		}
		return studentRepository.save(student);
	}

	// put fonction update data student
	@PutMapping("/students/{id}")
	public Student updateOne(@PathVariable(name = "id") Long id, @RequestBody Student student) {
		student.setId(id);
		return studentRepository.save(student);
	}

	// delete data
	@DeleteMapping("/students/{id}")
	public void delete(@PathVariable(name = "id") Long id) {
		studentRepository.deleteById(id);
	}

	/**
	 * la forme utilise par l interface est une forme HATEOS
	 * 
	 * par contre la forme utilise par controller est json
	 * 
	 */
}

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	CommandLineRunner start(StudentRepository studentRepository,
			RepositoryRestConfiguration repositoryRestConfiguration,
			LaboratoryRepository laboratoryRepository) {

		return args -> {
			repositoryRestConfiguration.exposeIdsFor(Student.class);
			Laboratory l1 = laboratoryRepository.save(new Laboratory(null, "info", "info@gmail.com", null));
			Laboratory l2 = laboratoryRepository.save(new Laboratory(null, "bio", "bio@gmail.com", null));
			studentRepository.save(new Student(null, "youssef", "testo5@gmail.com", new Date(), l1));
			studentRepository.save(new Student(null, "khalid", "testo1@gmail.com", new Date(), l1));
			studentRepository.save(new Student(null, "testo2", "testo2@gmail.com", new Date(), l2));
			studentRepository.save(new Student(null, "testo3", "testo3@gmail.com", new Date(), l2));

			// studentRepository.findAll().forEach(System.out::println);
			studentRepository.findAll().forEach(st -> {
				System.out.println(st.getName());
			});
		};
	}

}
