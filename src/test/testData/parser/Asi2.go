package main

func ASITests() int {
	// Valid ASI: Semicolon inserted after `return`
	// This actually returns nothing (if named return) or triggers an error
	// in the Go compiler if a value is expected, but the PARSER should parse it!
	/* return
	true
	*/

	// Valid line breaking for method chains
	x := Base().
		Method1().
		Method2()

	// Valid line breaking for binary operators
	y := 1 +
		2 +
		3

	return x + y
}

func Base() *TestObj { return nil }
type TestObj struct{}
func (t *TestObj) Method1() *TestObj { return t }
func (t *TestObj) Method2() int { return 0 }