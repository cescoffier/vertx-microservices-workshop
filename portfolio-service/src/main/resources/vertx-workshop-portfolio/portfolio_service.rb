require 'vertx/vertx'
require 'vertx/util/utils.rb'
# Generated from io.vertx.workshop.portfolio.PortfolioService
module VertxWorkshopPortfolio
  #  A service managing a portfolio.
  #  <p>
  #  This service is an event bus service (a.k.a service proxies, or async RPC). The client and server are generated at
  #  compile time.
  #  <p>
  #  All method are asynchronous and so ends with a  parameter.
  class PortfolioService
    # @private
    # @param j_del [::VertxWorkshopPortfolio::PortfolioService] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxWorkshopPortfolio::PortfolioService] the underlying java delegate
    def j_del
      @j_del
    end
    #  Gets the portfolio.
    # @yield the result handler called when the portfolio has been retrieved. The async result indicates whether the call was successful or not.
    # @return [void]
    def get_portfolio
      if block_given?
        return @j_del.java_method(:getPortfolio, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result != nil ? JSON.parse(ar.result.toJson.encode) : nil : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling get_portfolio()"
    end
    #  Buy `amount` shares of the given shares (quote).
    # @param [Fixnum] amount the amount
    # @param [Hash{String => Object}] quote the last quote
    # @yield the result handler with the updated portfolio. If the action cannot be executed, the async result is market as a failure (not enough money, not enough shares available...)
    # @return [void]
    def buy(amount=nil,quote=nil)
      if amount.class == Fixnum && quote.class == Hash && block_given?
        return @j_del.java_method(:buy, [Java::int.java_class,Java::IoVertxCoreJson::JsonObject.java_class,Java::IoVertxCore::Handler.java_class]).call(amount,::Vertx::Util::Utils.to_json_object(quote),(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result != nil ? JSON.parse(ar.result.toJson.encode) : nil : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling buy(amount,quote)"
    end
    #  Sell `amount` shares of the given shares (quote).
    # @param [Fixnum] amount the amount
    # @param [Hash{String => Object}] quote the last quote
    # @yield the result handler with the updated portfolio. If the action cannot be executed, the async result is market as a failure (not enough share...)
    # @return [void]
    def sell(amount=nil,quote=nil)
      if amount.class == Fixnum && quote.class == Hash && block_given?
        return @j_del.java_method(:sell, [Java::int.java_class,Java::IoVertxCoreJson::JsonObject.java_class,Java::IoVertxCore::Handler.java_class]).call(amount,::Vertx::Util::Utils.to_json_object(quote),(Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result != nil ? JSON.parse(ar.result.toJson.encode) : nil : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling sell(amount,quote)"
    end
    #  Evaluates the current value of the portfolio.
    # @yield the result handler with the valuation
    # @return [void]
    def evaluate
      if block_given?
        return @j_del.java_method(:evaluate, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling evaluate()"
    end
    # @param [::Vertx::Vertx] vertx 
    # @return [::VertxWorkshopPortfolio::PortfolioService]
    def self.get_proxy(vertx=nil)
      if vertx.class.method_defined?(:j_del) && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxWorkshopPortfolio::PortfolioService.java_method(:getProxy, [Java::IoVertxCore::Vertx.java_class]).call(vertx.j_del),::VertxWorkshopPortfolio::PortfolioService)
      end
      raise ArgumentError, "Invalid arguments when calling get_proxy(vertx)"
    end
  end
end
