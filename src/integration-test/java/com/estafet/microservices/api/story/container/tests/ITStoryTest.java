package com.estafet.microservices.api.story.container.tests;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.net.HttpURLConnection;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class ITStoryTest {

	@Before
	public void before() {
		RestAssured.baseURI = System.getenv("STORY_API_SERVICE_URI");
	}

	@After
	public void after() {
	}

	@Test
	public void testGetAPI() {
		get("/api").then()
			.body("id", is(1))
			.body("title", is("my story"))
			.body("description", is("my story description"))
			.body("storypoints", is(13))
			.body("sprintId", is(1))
			.body("projectId", is(1))
			.body("criteria.id", hasItems(1))
			.body("criteria.description", hasItems("my criterion"));
	}

	@Test
	@DatabaseSetup("ITStoryTest-data.xml")
	public void testGetStory() {
		get("/story/1000").then()
			.body("id", is(1000))
			.body("title", is("Story #1"))
			.body("description", is("Story #1"))
			.body("storypoints", is(13))
			.body("projectId", is(1))
			.body("status", is("Not Started"));
	}

	@Test
	@DatabaseSetup("ITStoryTest-data.xml")
	public void testCreateStory() {
		given()
			.contentType(ContentType.JSON)
			.body("{\"title\":\"My Story\",\"description\":\"My Story\",\"storypoints\":5}")
		.when()
			.post("/project/1/story")
		.then()
			.statusCode(HttpURLConnection.HTTP_OK)
			.body("id", is(1))
			.body("title", is("My Story"))
			.body("description", is("My Story"))
			.body("storypoints", is(5))
			.body("projectId", is(1))
			.body("status", is("Not Started"));
	
		get("/story/1").then()
			.body("id", is(1))
			.body("title", is("My Story"))
			.body("description", is("My Story"))
			.body("storypoints", is(5))
			.body("projectId", is(1))
			.body("status", is("Not Started"));
	}

	@Test
	@DatabaseSetup("ITStoryTest-data.xml")
	public void testGetStories() {
		get("/project/1/stories").then()
			.body("id", hasItems(1000))
			.body("title", hasItems("Story #1"))
			.body("description", hasItems("Story #1"))
			.body("status", hasItems("Not Started"));
	}

	@Test
	@DatabaseSetup("ITStoryTest-data.xml")
	public void testAddAcceptanceCriteria() {
		given()
			.contentType(ContentType.JSON)
			.body("{\"criteria\":\"Criteria #10\"}")
		.when()
			.post("/story/1000/criteria")
		.then()
			.statusCode(HttpURLConnection.HTTP_OK)
			.body("id", is(1000))
			.body("title", is("Story #1"))
			.body("description", is("Story #1"))
			.body("status", is("Not Started"))
			.body("criteria.description", hasItems("Criteria #10"));
	
		get("/story/1000").then()
			.body("id", is(1000))
			.body("title", is("Story #1"))
			.body("description", is("Story #1"))
			.body("status", is("Not Started"))
			.body("criteria.description", hasItems("Criteria #10"));
	}

	@Test
	@DatabaseSetup("ITStoryTest-data.xml")
	public void testAddSprintStory() {
		given()
			.contentType(ContentType.JSON)
			.body("{\"storyId\":1000,\"sprintId\":1}")
		.when()
			.post("/add-story-to-sprint")
		.then()
			.statusCode(HttpURLConnection.HTTP_OK)
			.body("id", is(1000))
			.body("title", is("Story #1"))
			.body("description", is("Story #1"))
			.body("status", is("In Progress"));
	
		get("/task/1001").then()
			.body("id", is(1000))
			.body("title", is("Story #1"))
			.body("description", is("Story #1"))
			.body("status", is("In Progress"));
	}

}
