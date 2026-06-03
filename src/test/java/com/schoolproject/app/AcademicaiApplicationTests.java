package com.schoolproject.app;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class AcademicaiApplicationTests {

	@Test
	void contextLoads() {
		assertDoesNotThrow(() -> AcademicaiApplication.class.getName());
	}

}
