package main

func Variadics() {
	// 1. Trailing comma in Slice literal
	items := []int{
		1,
		2,
		3,
	}

	// 2. Trailing comma in function call arguments
	items = append(
		items,
		4,
		5,
	)

	// 3. Variadic unpacking (`...`) in function calls
	more := []int{6, 7}
	items = append(items, more...)

	// 4. Map literals with trailing commas
	m := map[string]int{
		"a": 1,
		"b": 2,
	}
	_ = m
}