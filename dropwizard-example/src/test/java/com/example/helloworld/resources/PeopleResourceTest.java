package com.example.helloworld.resources;

import com.example.helloworld.core.Person;
import com.example.helloworld.db.PersonDAO;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PeopleResource}.
 */
@ExtendWith(DropwizardExtensionsSupport.class)
public class PeopleResourceTest {
    private static final PersonDAO PERSON_DAO = mock(PersonDAO.class);
    public static final ResourceExtension RESOURCES = ResourceExtension.builder()
            .addResource(new PeopleResource(PERSON_DAO))
            .build();
    private ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
    private Person person;

    @BeforeEach
    public void setUp() {
        person = new Person();
        person.setFullName("Full Name");
        person.setJobTitle("Job Title");
    }

    @AfterEach
    public void tearDown() {
        reset(PERSON_DAO);
    }

    @Test
    public void createPerson() throws JsonProcessingException {
        when(PERSON_DAO.create(any(Person.class))).thenReturn(person);
        final Response response = RESOURCES.target("/people")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(person, MediaType.APPLICATION_JSON_TYPE));

        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.OK);
        verify(PERSON_DAO).create(personCaptor.capture());
        assertThat(personCaptor.getValue()).isEqualTo(person);
    }

    @Test
    public void listPeople() throws Exception {
        final List<Person> people = Collections.singletonList(person);
        when(PERSON_DAO.findAll()).thenReturn(people);

        final List<Person> response = RESOURCES.target("/people")
            .request().get(new GenericType<List<Person>>() {
            });

        verify(PERSON_DAO).findAll();
        assertThat(response).containsAll(people);
    }
}
