package main

func ControlFlowTests() {
	// 1. Empty blocks (Tests your `{pin=1}` and `recoverWhile` rules)
	if true {}
	for {}
	switch {}
	select {}

	// 2. Init statements in control flow
	if x := 1; x > 0 {
		x++
	}

	// 3. Range loops (Tests your ShortVarDecl vs Assignment fixes)
	for i, v := range []int{1, 2, 3} {
		_, _ = i, v
	}
	for range []int{1} {} // No variables

	// 4. Expression Switch vs Type Switch
	switch x := 1; x {
	case 1, 2, 3:
		break
	default:
		fallthrough
	}

	var i any
	switch v := i.(type) {
	case int:
	case string, bool:
	default:
	}
}