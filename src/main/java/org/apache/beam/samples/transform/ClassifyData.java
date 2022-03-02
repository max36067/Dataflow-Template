package org.apache.beam.samples.transform;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.TupleTag;
import org.apache.beam.samples.model.CustomData;
import org.joda.time.Instant;

import java.util.Objects;


public class ClassifyData extends DoFn<CustomData, CustomData> {
    private final TupleTag<String> errorTag;
    private final TupleTag<CustomData> oddTag;

    public ClassifyData(TupleTag<CustomData> oddTag, TupleTag<String> errorTag) {
        this.errorTag = errorTag;
        this.oddTag = oddTag;
    }

    @ProcessElement
    public void processElement(ProcessContext context) {
        CustomData tmpObj = new CustomData();
        try {
            CustomData element = Objects.requireNonNull(context.element());
            tmpObj.count = element.count;
            tmpObj.processingTime = new Instant();

            if (tmpObj.count % 2 == 0) {
                tmpObj.type = "even";
                System.out.printf("Obj Type: %s, Obj count: %d %n", tmpObj.type, tmpObj.count);
                context.outputWithTimestamp(tmpObj, tmpObj.processingTime);
            } else {
                tmpObj.type = "odd";
                System.out.printf("Obj Type: %s, Obj count: %d %n", tmpObj.type, tmpObj.count);
                context.outputWithTimestamp(oddTag, tmpObj, tmpObj.processingTime);
            }

        } catch (Exception p) {
            System.out.printf("%s %n", p);
            context.output(errorTag, p.toString());
        }
        ;
    }
}
