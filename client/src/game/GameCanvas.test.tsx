import { mod } from "./GameCanvas";

test("mod function tests", () => {
  expect(mod(4, 3)).toBe(1);
  expect(mod(4, 2)).toBe(0);
  expect(mod(3, 4)).toBe(3);
  expect(mod(-4, 3)).toBe(2);
});
