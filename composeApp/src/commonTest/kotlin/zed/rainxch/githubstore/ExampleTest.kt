package zed.rainxch.githubstore

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ExampleTest : FunSpec({
    test("my first test") {
        val result = 1 + 2
        result shouldBe 2
    }

    test("another test") {
        "hello".uppercase() shouldBe "HELLO"
    }
})