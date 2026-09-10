// Copyright (c) 2011-2025 ScalaMock Contributors (https://github.com/ScalaMock/ScalaMock/graphs/contributors)
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package org.scalamock.kyotest.macros

import org.scalamock.kyotest.ScalamockKyoSyntaxBase
import org.scalamock.kyotest.syntax.MockConvertions

/**
 * Allows configuring mocks outside of test classes, if needed:
 *
 * {{{
 * object MyUtils extends ScalamockKyoSyntax {
 *   def mockGetName = Kyo.serviceWith[UserService] { mock =>
 *     (mock.getName _).expects(4).returnsKyo("Agent Smith")
 *   }
 * }
 * }}}
 *
 * Creating mocks and configuring Ordering can only be done in test code.
 * The rest of the API is available through mixing in `ScalamockKyoSyntax`.
 */
trait ScalamockKyoSyntax extends ScalamockKyoSyntaxBase with MockConvertions
