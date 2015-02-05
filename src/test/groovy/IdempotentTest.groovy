import groovyx.gpars.GParsPool
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicInteger


class IdempotentTest extends Specification {
    def "synchronous calls returning nothing are processed only 16 times"() {
        given:
        def ut = new UsesIdempotent()
        def processCount = new AtomicInteger(0)
        def totalCount = 10000

        when:
        totalCount.times {
            ut.process(UUID.randomUUID().toString()) {
                processCount.andIncrement
            }
        }

        then:
        processCount.get() == 16
    }

    def "synchronous calls returning 1 are processed only 16 times but return everytime it's called"() {
        given:
        def ut = new UsesIdempotent<Integer>()
        def processCount = new AtomicInteger(0)
        def returnCount = 0
        def totalCount = 10000

        when:
        totalCount.times {
            returnCount += ut.process(UUID.randomUUID().toString()) {
                processCount.andIncrement
                return 1
            }
        }

        then:
        processCount.get() == 16
        returnCount == totalCount
    }

    def "concurrent calls are processed only 16 times"() {
        given:
        def ut = new UsesIdempotent()
        def processCount = new AtomicInteger(0)
        def totalCount = 1000000

        when:
        GParsPool.withPool {
            (1..totalCount).eachParallel {
                ut.process(UUID.randomUUID().toString()) {
                    processCount.andIncrement
                }
            }
        }

        then:
        processCount.get() == 16
    }

    def "concurrent call returning 1 are processed only 16 times but return everytime it's called"() {
        given:
        def ut = new UsesIdempotent<Integer>()
        def processCount = new AtomicInteger(0)
        def returnCount = new AtomicInteger(0)
        def totalCount = 1000000

        when:
        GParsPool.withPool {
            (1..totalCount).eachParallel {
                returnCount.addAndGet(ut.process(UUID.randomUUID().toString()) {
                    processCount.andIncrement
                    return 1
                })
            }
        }

        then:
        processCount.get() == 16
        returnCount.get() == totalCount
    }

}


class UsesIdempotent<T> implements Idempotent<T> {
    T process(String input, Closure<T> action) {
        return idempotent(input[0], action)
    }
}