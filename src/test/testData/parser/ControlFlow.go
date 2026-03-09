package main

func main() {
    if x > 0 {
        println("Positive")
    } else if x < 0 {
        println("Negative")
    } else {
        println("Zero")
    }

    for i := 0; i < 10; i++ {
        if i == 5 {
            continue
        }
        println(i)
    }

    switch x {
    case 1:
        println("one")
    default:
        println("other")
    }
}