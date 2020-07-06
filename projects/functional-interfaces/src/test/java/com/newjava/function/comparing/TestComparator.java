package com.newjava.function.comparing;

import com.google.common.collect.Lists;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestComparator {

    @Test
    void testFunctionalComparator() {
        Comparator<Integer> intDesc = (o1, o2) -> o2 - o1;
        List<Integer> list = Lists.newArrayList(1, 2, 3);
        list.sort(intDesc);
        assertEquals(Lists.newArrayList(3, 2, 1), list);
    }

    class Person {
        private String name;
        private int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    @Test
    void testThenComparingToComparator() {
        Comparator<Person> comparingByName = (person1, person2) -> person1.getName().compareTo(person2.getName());
        Comparator<Person> comparingByAge = (person1, person2) -> person1.getAge() - person2.getAge();

        List<Person> people = Lists.newArrayList(
                new Person("Zhang si", 20),
                new Person("Zhang san", 25),
                new Person("Wang er", 25)
        );

        people.sort(comparingByName.thenComparing(comparingByAge));
        assertEquals("Wang er", people.get(0).getName());

        people.sort(comparingByAge.thenComparing(comparingByName));
        assertEquals("Zhang si", people.get(0).getName());

        people.sort(comparingByAge.reversed().thenComparing(comparingByName.reversed()));
        assertEquals("Zhang san", people.get(0).getName());
    }

    @Test
    void testThenComparingWithComparing() {
        List<Person> people = Lists.newArrayList(
                new Person("Zhang si", 20),
                new Person("Zhang san", 25),
                new Person("Wang er", 25)
        );
        Comparator<Person> comparingByName = Comparator.comparing(Person::getName, Comparator.naturalOrder());

        Comparator<Person> comparingByAge = Comparator.comparingInt(Person::getAge);

        people.sort(comparingByName.thenComparing(Person::getAge, Comparator.naturalOrder()));
        assertEquals("Wang er", people.get(0).getName());

        people.sort(comparingByAge.thenComparing(Person::getName, Comparator.naturalOrder()));
        assertEquals("Zhang si", people.get(0).getName());

        people.sort(comparingByAge.reversed().thenComparing(Person::getName, Comparator.reverseOrder()));
        assertEquals("Zhang san", people.get(0).getName());
    }
}
