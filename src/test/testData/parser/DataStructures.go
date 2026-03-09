package main

func DataStructures() {
	// Arrays and Slices
	arr := [...]int{1, 2, 3}
	slice := []string{"a", "b"}

	// Slice expressions
	sub1 := arr[1:2]
	sub2 := arr[:2]
	sub3 := arr[1:]
	sub4 := arr[0:2:3] // Full slice expression with capacity

	// Maps
	m := map[string]int{
		"one": 1,
		"two": 2, // Note: Trailing comma is required by ASI here
	}

	// Nested Struct Literals
	u := User{
		Name: "Alice",
		Age:  30,
		Address: &Address{
			City: "Wonderland",
		},
	}
}