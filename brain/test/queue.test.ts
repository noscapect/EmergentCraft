import test from "node:test";
import assert from "node:assert/strict";
import { AsyncQueue } from "../src/queue.js";

test("global inference queue runs only its configured concurrency", async () => {
  const queue = new AsyncQueue(1); const order: string[] = []; let release!: () => void;
  const gate = new Promise<void>(resolve => { release = resolve; });
  const first = queue.run(async () => { order.push("first-start"); await gate; order.push("first-end"); });
  const second = queue.run(async () => { order.push("second-start"); });
  await new Promise(resolve => setImmediate(resolve));
  assert.deepEqual(order, ["first-start"]); assert.equal(queue.queued, 1);
  release(); await Promise.all([first, second]); assert.deepEqual(order, ["first-start", "first-end", "second-start"]);
});
