package main

func Jumps() {
	i := 0

// Label declaration
OuterLoop:
	for i < 10 {
		for j := 0; j < 10; j++ {
			if i+j > 15 {
				// Break to specific label
				break OuterLoop
			}
			if i == j {
				// Continue to specific label
				continue OuterLoop
			}
			goto Skip
		}
		i++
	}

Skip:
	println("Skipped!")
}