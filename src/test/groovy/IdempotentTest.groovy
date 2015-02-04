import groovyx.gpars.GParsPool
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicInteger


class IdempotentTest extends Specification {
    def "synchronous calls"() {
        given:
        def ut = new UsesIdempotent()
        def processCount = new AtomicInteger(0)

        when:
        10000.times {
            ut.process(UUID.randomUUID().toString()) {
                processCount.andIncrement
            }
        }

        then:
        processCount.get() == 16
    }

    def "concurrent calls"() {
        given:
        def ut = new UsesIdempotent()
        def processCount = new AtomicInteger(0)

        when:
        GParsPool.withPool {
            (0..1000000).eachParallel{
                ut.process(UUID.randomUUID().toString()) {
                    processCount.andIncrement
                }
            }
        }

        then:
        processCount.get() == 16
    }

}


class UsesIdempotent implements Idempotent {
    void process(String input, Closure action) {
        idempotent(input[0], action)
    }
}