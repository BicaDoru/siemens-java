package com.siemens.internship;

import com.siemens.internship.model.Item;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InternshipApplicationTests {
	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void createItemWithValidEmail() {
		Item item = new Item(
				null,
				"name",
				"description",
				"status",
				"email@example.com");

		ResponseEntity<Item> response = restTemplate.postForEntity("/api/items", item, Item.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getEmail()).isEqualTo("email@example.com");
	}

	@Test
	void createItemWithSubdomainEmail() {
		Item item = new Item(
				null,
				"name",
				"description",
				"status",
				"name@subdomain.example.com");

		ResponseEntity<Item> response = restTemplate.postForEntity("/api/items", item, Item.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getEmail()).isEqualTo("name@subdomain.example.com");
	}

	@Test
	void updateItemWithValidFields() {
		Item item = new Item(
				null,
				"name",
				"description",
				"status",
				"email@example.com");

		ResponseEntity<Item> postResponse = restTemplate.postForEntity("/api/items", item, Item.class);

		assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		Long id = postResponse.getBody().getId();

		Item updatedItem = new Item(
				null,
				"updated name",
				"updated description",
				"updated status",
				"email@example.com");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Item> request = new HttpEntity<>(updatedItem, headers);

		ResponseEntity<Item> putResponse = restTemplate.exchange("/api/items/" + id, HttpMethod.PUT, request, Item.class);

		assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(putResponse.getBody()).isNotNull();
		assertThat(putResponse.getBody().getId()).isEqualTo(id);
		assertThat(putResponse.getBody().getName()).isEqualTo("updated name");
		assertThat(putResponse.getBody().getDescription()).isEqualTo("updated description");
		assertThat(putResponse.getBody().getStatus()).isEqualTo("updated status");
		assertThat(putResponse.getBody().getEmail()).isEqualTo("email@example.com");
	}

	@Test
	void deleteItemWithValidId() {
		Item item = new Item(
				null,
				"name",
				"description",
				"status",
				"email@example.com");

		ResponseEntity<Item> postResponse = restTemplate.postForEntity("/api/items", item, Item.class);

		assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		Long id = postResponse.getBody().getId();

		ResponseEntity<Void> deleteResponse = restTemplate.exchange("/api/items/" + id, HttpMethod.DELETE, null, Void.class);
		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<Item> getResponse = restTemplate.getForEntity("/api/items/" + id, Item.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void createItemWithInvalidEmailFormat() {
		Item item = new Item(
				null,
				"name",
				"description",
				"status",
				"invalid-email-format");

		ResponseEntity<String> response = restTemplate.postForEntity("/api/items", item, String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).contains("Invalid email format");
	}

	@Test
	void createItemWithConsecutiveDotsInEmail() {
		Item item = new Item(
				null,
				"name",
				"description",
				"status",
				"user..name@domain.com");

		ResponseEntity<String> response = restTemplate.postForEntity("/api/items", item, String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).contains("Invalid email format");
	}

	@Test
	void createItemWithEmptyField() {
		Item item = new Item(
				null,
				"",
				"description",
				"status",
				"email@example.com");

		ResponseEntity<String> response = restTemplate.postForEntity("/api/items", item, String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).contains("must not be empty");
	}

	@Test
	void createItemWithMissingDomainInEmail() {
		Item item = new Item(
				null,
				"name",
				"description",
				"status",
				"user@");

		ResponseEntity<String> response = restTemplate.postForEntity("/api/items", item, String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).contains("Invalid email format");
	}

	@Test
	void createItemWithSpecialCharBeforeAtInEmail() {
		Item item = new Item(
				null,
				"name",
				"description",
				"status",
				"test.@example.com");

		ResponseEntity<String> response = restTemplate.postForEntity("/api/items", item, String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).contains("Invalid email format");
	}

	@Test
	void getItemByIdSuccess() {
		Item item = new Item(
				null,
				"name",
				"description",
				"status",
				"email@example.com");

		ResponseEntity<Item> postResponse = restTemplate.postForEntity("/api/items", item, Item.class);

		assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		Item createdItem = postResponse.getBody();
		assertThat(createdItem).isNotNull();

		ResponseEntity<Item> getResponse = restTemplate.getForEntity("/api/items/" + createdItem.getId(), Item.class);

		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(getResponse.getBody()).isNotNull();
		assertThat(getResponse.getBody().getId()).isEqualTo(createdItem.getId());
		assertThat(getResponse.getBody().getName()).isEqualTo("name");
		assertThat(getResponse.getBody().getDescription()).isEqualTo("description");
		assertThat(getResponse.getBody().getStatus()).isEqualTo("status");
		assertThat(getResponse.getBody().getEmail()).isEqualTo("email@example.com");
	}

	@Test
	void getItemByIdNotFound() {
		ResponseEntity<Item> response = restTemplate.getForEntity("/api/items/9999", Item.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
}
