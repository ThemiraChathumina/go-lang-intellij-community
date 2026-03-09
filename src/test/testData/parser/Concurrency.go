package main

func Concurrency() {
	ch := make(chan int, 10)
	done := make(chan struct{})

	// Go routine with anonymous function
	go func() {
		ch <- 42 // Send
	}()

	// Select statement with receive and default
	select {
	case val := <-ch: // Receive with assignment
		println(val)
	case <-done:      // Receive only
		return
	default:
		println("blocking")
	}
}

// Channel types (send-only, receive-only, bidirectional)
func ChanTypes(a chan int, b <-chan string, c chan<- bool) {
	close(a)
}